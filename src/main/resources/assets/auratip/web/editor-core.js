// -----------------------------
// WebSocket / connection
// -----------------------------

function getWsPort() {
  const qs = new URLSearchParams(location.search);
  const p = qs.get('wsPort');
  if (p) return p;
  if (location.port) return location.port;
  if (INJECTED_WS_PORT && INJECTED_WS_PORT !== '__AURATIP_WS_PORT__') return INJECTED_WS_PORT;
  return '0';
}

function getWsUrl() {
  const port = getWsPort();
  const host = location.host ? location.host : `127.0.0.1:${port}`;
  return `ws://${host}/ws`;
}

function send(obj) {
  if (!ws || ws.readyState !== WebSocket.OPEN) return;
  ws.send(JSON.stringify(obj));
}

function sendLiveDebounced() {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => sendLiveNow(), 60);
}

function sendLiveNow() {
  if (!mode) return;
  if (mode === 'radial') send({ type: 'radial_update', menu: radialState });
  else send({ type: 'tip_update', tip: tipState });
}

// -----------------------------
// JSON Inspector (single place for raw json)
// -----------------------------

function currentStateObject() {
  if (mode === 'radial') return radialState;
  if (mode === 'tip') return tipState;
  return tipState || radialState;
}

function refreshJsonArea(force) {
  if (!jsonArea) return;
  if (jsonDirty && !force) return;
  const obj = currentStateObject();
  const next = JSON.stringify(obj || {}, null, 2);
  if (!force && next === jsonLastRendered) return;
  jsonArea.value = next;
  jsonLastRendered = next;
  jsonDirty = false;
  if (jsonStatus) jsonStatus.textContent = 'READY';
}

function parseJsonArea() {
  const text = jsonArea.value || '';
  const v = safeJsonParse(text);
  if (v === null) {
    if (jsonStatus) jsonStatus.textContent = 'INVALID JSON';
    return null;
  }
  if (jsonStatus) jsonStatus.textContent = 'VALID';
  return v;
}

// -----------------------------
// Animation editor
// -----------------------------

function normalizeAnimId(raw) {
  const s = String(raw || '').trim();
  if (!s) return (animKind.value === 'hover') ? 'nekojs:hover_animation' : 'nekojs:transition_animation';
  if (s.indexOf(':') < 0) return `nekojs:${s}`;
  return s;
}

function buildAnimJavaSnippet() {
  const kind = animKind.value === 'hover' ? 'hover' : 'transition';
  const id = normalizeAnimId(animId.value);
  const defs = safeJsonParse(animDefaults.value || '{}');
  const defaultsObj = (defs && typeof defs === 'object') ? defs : {};

  const getters = [];
  Object.keys(defaultsObj).forEach(k => {
    const v = defaultsObj[k];
    if (typeof v === 'boolean') getters.push(`    boolean ${k} = params.getBoolean(${JSON.stringify(k)}, ${v});`);
    else if (typeof v === 'number') getters.push(`    double ${k} = params.getDouble(${JSON.stringify(k)}, ${v});`);
    else getters.push(`    String ${k} = params.getString(${JSON.stringify(k)}, ${JSON.stringify(String(v))});`);
  });

  const lines = [];
  const push = (s = '') => lines.push(s);

  push(`// AuraTip Tip animation (${kind}) (Java)`);
  push('import cc.sighs.auratip.api.animation.TipAnimations;');
  push(`import cc.sighs.auratip.api.animation.${kind === 'hover' ? 'HoverAnimation' : 'TransitionAnimation'};`);
  push('import net.minecraft.resources.ResourceLocation;');
  push('');

  if (kind === 'hover') {
    push(`TipAnimations.registerHover(new ResourceLocation(${JSON.stringify(id)}), params -> {`);
  } else {
    push(`TipAnimations.register(new ResourceLocation(${JSON.stringify(id)}), params -> {`);
  }

  if (getters.length) {
    getters.forEach(l => push(l));
    push('');
  }

  if (kind === 'hover') {
    push('  return new HoverAnimation() {');
    push('    @Override public int offsetX(long now, long start, int w, int h, float speed) {');
    push('      return 0;');
    push('    }');
    push('    @Override public int offsetY(long now, long start, int w, int h, float speed) {');
    push('      return 0;');
    push('    }');
    push('  };');
  } else {
    push('  return new TransitionAnimation() {');
    push('    @Override public float easedProgress(long nowMs, long startMs, boolean closing, int openMs, int closeMs) {');
    push('      return 0.0f;');
    push('    }');
    push('    @Override public int offsetX(float eased, int panelWidth, int panelHeight) {');
    push('      return 0;');
    push('    }');
    push('    @Override public int offsetY(float eased, int panelWidth, int panelHeight) {');
    push('      return 0;');
    push('    }');
    push('  };');
  }
  push('});');

  return lines.join('\n') + '\n';
}

function refreshAnimJavaSnippet(force) {
  const next = buildAnimJavaSnippet();
  if (force || (!animJavaDirty && (!animJs.value || animJs.value === animJavaLastGenerated))) {
    animJs.value = next;
    animJavaDirty = false;
  }
  animJavaLastGenerated = next;
}

// -----------------------------
// Export
// -----------------------------

function toggleExportMenu() {
  exportMenu.classList.toggle('hidden');
}

function exportCurrentJson() {
  const obj = (mode === 'radial') ? radialState : tipState;
  const name = (mode === 'radial') ? 'auratip_radial_menu.json' : 'auratip_tip.json';
  downloadText(name, JSON.stringify(obj || {}, null, 2), 'application/json;charset=utf-8');
}

function exportKubeJs() {
  const lines = [];
  const push = (s = '') => lines.push(s);

  function compExpr(componentJson) {
    if (componentJson === null || componentJson === undefined) return 'null';
    if (typeof componentJson === 'string') {
      return `TipText.of(${JSON.stringify(componentJson)}).build()`;
    }
    if (isPlainObject(componentJson)) {
      const clean = sanitizeComponentJson(componentJson);
      const keys = Object.keys(clean);
      const styleKeys = new Set(['color', 'bold', 'italic', 'underlined', 'strikethrough', 'obfuscated', 'extra']);

      if (keys.length === 1 && keys[0] === 'text') {
        return `TipText.of(${JSON.stringify(clean.text || '')}).build()`;
      }

      const hasUnsupported = keys.some(k => !(k === 'text' || k === 'translate' || k === 'with' || styleKeys.has(k)));
      const hasText = Object.prototype.hasOwnProperty.call(clean, 'text');
      const hasTranslate = Object.prototype.hasOwnProperty.call(clean, 'translate');

      if (!hasUnsupported && (hasText || hasTranslate)) {
        let expr;
        if (hasText) {
          expr = `TipText.of(${JSON.stringify(clean.text || '')})`;
        } else {
          const args = Array.isArray(clean.with)
            ? clean.with.map(a => {
              if (a === null || a === undefined) return 'null';
              if (typeof a === 'number' || typeof a === 'boolean') return String(a);
              if (typeof a === 'string') return JSON.stringify(a);
              return compExpr(a);
            }).join(', ')
            : '';
          expr = `TipText.translatable(${JSON.stringify(String(clean.translate || ''))}${args ? ', ' + args : ''})`;
        }

        if (typeof clean.color === 'string' && clean.color.length > 0) {
          if (clean.color.startsWith('#')) expr += `.colorHex(${JSON.stringify(clean.color)})`;
          else expr += `.formatting(${JSON.stringify(clean.color)})`;
        }
        if (clean.bold) expr += `.bold()`;
        if (clean.italic) expr += `.italic()`;
        if (clean.underlined) expr += `.underlined()`;
        if (clean.strikethrough) expr += `.strikethrough()`;
        if (clean.obfuscated) expr += `.obfuscated()`;

        if (Array.isArray(clean.extra)) {
          for (const part of clean.extra) {
            expr += `.appendComponent(${compExpr(part)})`;
          }
        }

        return `${expr}.build()`;
      }

      const raw = JSON.stringify(clean);
      return `Java.loadClass('net.minecraft.network.chat.Component$Serializer').fromJson(${JSON.stringify(raw)})`;
    }

    const raw = JSON.stringify(componentJson);
    return `Java.loadClass('net.minecraft.network.chat.Component$Serializer').fromJson(${JSON.stringify(raw)})`;
  }

  function actionExpr(actionObj) {
    const a = (actionObj && typeof actionObj === 'object') ? actionObj : { type: 'auratip:run_command', command: '/say hello' };
    const type = a.type || 'auratip:run_command';
    const params = { ...a };
    delete params.type;
    const keys = Object.keys(params);
    if (keys.length === 0) {
      return `Actions.of(${JSON.stringify(type)})`;
    }
    return `Actions.of(${JSON.stringify(type)}, ${JSON.stringify(params)})`;
  }

  if (mode === 'radial') {
    const m = radialState || {};
    const ms = m.menu_settings || {};
    const slots = Array.isArray(m.slots) ? m.slots : [];

    push('// AuraTip Radial Menu (NekoJS)');
    push('// Put this in: nekojs/client_scripts/*.js');
    push('');
    push('RadialMenuEvents.register(event => {');
    push(`  const menu = event.create(${JSON.stringify(String(m.id || 'nekojs:radial_menu'))});`);
    push('');
    push(`  menu.radii(${Number(ms.inner_radius ?? 55)}, ${Number(ms.outer_radius ?? 100)});`);
    push(`  menu.animationSpeed(${Number(ms.animation_speed ?? 1.0)});`);
    if (ms.ring_color) push(`  menu.ringColor(${JSON.stringify(ms.ring_color)});`);
    if (ms.ring_colors) push(`  menu.ringColors(${JSON.stringify(ms.ring_colors)});`);
    if (ms.center_icon) push(`  menu.centerIcon(${JSON.stringify(ms.center_icon)});`);
    if (ms.close_key) push(`  menu.closeKey(${JSON.stringify(ms.close_key)});`);
    push('');

    slots.forEach(s => {
      const name = String(s.name || 'Slot');
      const icon = s.icon;
      if (icon && typeof icon === 'object' && !Array.isArray(icon)) {
        // Item icon → slotItem(name, ItemStackTemplate, ...)
        const tmpl = (icon.stack && typeof icon.stack === 'object') ? icon.stack
                   : (icon.stack  && typeof icon.stack  === 'object') ? icon.stack
                   : icon;
        const itemId = String(tmpl.id || 'minecraft:air');
        const count = Number(tmpl.Count || 1) | 0;
        const icoScale = Number(icon.scale || 1.0);
        // Build template JSON for ItemStackUtil.fromJson()
        const tmplJson = JSON.stringify({
          id: tmpl.id || 'minecraft:air',
          count: tmpl.Count || 1,
          components: tmpl.tag || {}
        });
        push('  menu.slotItem(');
        push(`    ${JSON.stringify(name)},`);
        push(`    new ItemIcon(ItemStackUtil.fromJson(${JSON.stringify(tmplJson)}), ${icoScale}),`);
        push(`    ${actionExpr(s.action)},`);
        push(`    ${s.text ? compExpr(s.text) : 'null'},`);
        push(`    ${s.highlight_color ? JSON.stringify(s.highlight_color) : 'null'}`);
        push('  );');
      } else {
        // Texture icon → slot(name, TextureIcon, ...)
        let texScale = 1.0;
        let texId = String(icon || 'minecraft:textures/item/paper.png');
        // object format {id, scale} from editor schema
        if (typeof icon === 'object' && icon !== null) {
          texId = String(icon.id || 'minecraft:textures/item/paper.png');
          texScale = Number(icon.scale || 1.0);
        }
        push('  menu.slotWithIcon(');
        push(`    ${JSON.stringify(name)},`);
        push(`    new TextureIcon(Identifier.parse(${JSON.stringify(texId)}), ${texScale}),`);
        push(`    ${actionExpr(s.action)},`);
        push(`    ${s.text ? compExpr(s.text) : 'null'},`);
        push(`    ${s.highlight_color ? JSON.stringify(s.highlight_color) : 'null'}`);
        push('  );');
      }
      push('');
    });

    push('});');
    downloadText('auratip_radial_menu.kubejs.js', lines.join('\n') + '\n', 'text/javascript;charset=utf-8');
    return;
  }

  const tData = tipState || {};
  const trig = tData.trigger || {};
  const vs = tData.visual_settings || {};
  const beh = tData.behavior || {};
  const pages = Array.isArray(tData.pages) ? tData.pages : [];

  push('// AuraTip Tip (NekoJS)');
  push('// Put this in: nekojs/client_scripts/*.js');
  push('');
  push('TipEvents.register(event => {');
  push(`  const tip = event.create(${JSON.stringify(String(tData.id || 'nekojs:tip'))});`);
  push(`  tip.trigger(${JSON.stringify(String(trig.type || 'auratip:first_join_world'))}, ${JSON.stringify(String(trig.mode || 'once'))}, ${Number(trig.cooldown || 0)});`);
  push('');

  push('  tip.visual(v => {');
  if (vs.animation_style) push(`    v.animationStyle(${JSON.stringify(vs.animation_style)});`);
  if (vs.animation_speed != null) push(`    v.animationSpeed(${Number(vs.animation_speed)});`);
  if (vs.animation_params && typeof vs.animation_params === 'object' && Object.keys(vs.animation_params).length) push(`    v.animParams(${JSON.stringify(vs.animation_params)});`);
  if (vs.hover_animation_style) push(`    v.hoverAnimationStyle(${JSON.stringify(vs.hover_animation_style)});`);
  if (vs.hover_animation_speed != null) push(`    v.hoverAnimationSpeed(${Number(vs.hover_animation_speed)});`);
  if (vs.hover_only_on_hover != null) push(`    v.hoverOnlyOnHover(${!!vs.hover_only_on_hover});`);
  if (vs.hover_animation_params && typeof vs.hover_animation_params === 'object' && Object.keys(vs.hover_animation_params).length) push(`    v.hoverParams(${JSON.stringify(vs.hover_animation_params)});`);
  if (vs.stripe_width != null) push(`    v.stripeWidth(${Number(vs.stripe_width) | 0});`);
  if (vs.stripe_length_factor != null) push(`    v.stripeLengthFactor(${Number(vs.stripe_length_factor)});`);
  if (vs.theme_color) push(`    v.themeColor(${JSON.stringify(vs.theme_color)});`);
  if (vs.width != null && vs.height != null) push(`    v.size(${Number(vs.width) | 0}, ${Number(vs.height) | 0});`);

  if (typeof vs.position === 'string') push(`    v.position(${JSON.stringify(vs.position)});`);
  if (vs.layout && typeof vs.layout === "object") {
    const lay = vs.layout;
    if (lay.padding && typeof lay.padding === "object") {
      const p = lay.padding;
      push(`    v.padding(${Number(p.top||12)}, ${Number(p.right||12)}, ${Number(p.bottom||12)}, ${Number(p.left||12)});`);
    } else if (typeof lay.padding === "number") {
      push(`    v.padding(${lay.padding});`);
    }
    if (lay.element_spacing != null) push(`    v.elementSpacing(${Number(lay.element_spacing) | 0});`);
  }
  if (Array.isArray(vs.position)) push(`    v.position(${Number(vs.position[0] || 0)}, ${Number(vs.position[1] || 0)});`);

  if (vs.animation_from !== undefined) {
    if (typeof vs.animation_from === 'string') push(`    v.animationFrom(${JSON.stringify(vs.animation_from)});`);
    if (Array.isArray(vs.animation_from)) push(`    v.animationFrom(${Number(vs.animation_from[0] || 0)}, ${Number(vs.animation_from[1] || 0)});`);
  }
  if (vs.animation_to !== undefined) {
    if (typeof vs.animation_to === 'string') push(`    v.animationTo(${JSON.stringify(vs.animation_to)});`);
    if (Array.isArray(vs.animation_to)) push(`    v.animationTo(${Number(vs.animation_to[0] || 0)}, ${Number(vs.animation_to[1] || 0)});`);
  }

  if (vs.background && typeof vs.background === 'object') {
    const bg = vs.background;
    push(`    v.background(${JSON.stringify(String(bg.type || 'gradient'))}, ${JSON.stringify(bg.colors || [])}, ${Number(bg.border_radius || 8) | 0});`);
    if (bg.rounded != null) push(`    v.backgroundRounded(${!!bg.rounded});`);
    if (bg.image_path) push(`    v.backgroundImage(${JSON.stringify(bg.image_path)});`);
    if (bg.shadow && typeof bg.shadow === "object") {
      const sh = bg.shadow;
      push(`    v.shadow(${!!sh.enabled}, ${JSON.stringify(String(sh.color || "#8C000000"))}, ${Number(sh.offset_x||2)}, ${Number(sh.offset_y||2)}, ${Number(sh.size||4)});`);
    }
  }
  push('  });');
  push('');

  push('  tip.behavior(b => {');
  if (beh.default_duration != null) push(`    b.duration(${Number(beh.default_duration) | 0});`);
  if (beh.pause_timer_on_hover != null) push(`    b.pauseOnHover(${!!beh.pause_timer_on_hover});`);
  if (beh.closable_by_key) push(`    b.closeKey(${JSON.stringify(beh.closable_by_key)});`);
  if (beh.allow_paging != null) push(`    b.allowPaging(${!!beh.allow_paging});`);
    if (beh.show_close_button != null) push(`    b.showCloseButton(${!!beh.show_close_button});`);
    if (beh.show_page_indicator != null) push(`    b.showPageIndicator(${!!beh.show_page_indicator});`);
  push('  });');
  push('');

  pages.forEach(p => {
    const idx = Number(p.page_index || 0);
    push(`  tip.page(${idx}, pg => {`);
    const title = p.title;
    const subtitle = p.subtitle;
    const content = p.content;

    if (title && title.text) push(`    pg.title(${compExpr(title.text)}, ${Number(title.scale || 1.0)}, ${Number(title.line_spacing || 0) | 0});`);
    if (title && title.divider) {
      const d = title.divider;
      push(`    pg.titleDivider(${Number(d.thickness || 1) | 0}, ${Number(d.margin_top || 4) | 0}, ${Number(d.margin_bottom || 4) | 0}, ${Number(d.length || 1.0)}, ${JSON.stringify(d.color || '')});`);
    }
    if (subtitle && subtitle.text) push(`    pg.subtitle(${compExpr(subtitle.text)}, ${Number(subtitle.scale || 1.0)}, ${Number(subtitle.line_spacing || 0) | 0});`);
    if (content && content.text) push(`    pg.content(${compExpr(content.text)}, ${Number(content.scale || 1.0)}, ${Number(content.line_spacing || 0) | 0});`);

    if (p.image && p.image.path) {
      const img = p.image;
      const size = Array.isArray(img.size) ? img.size : [64, 64];
      const w = Number(size[0] || 64) | 0;
      const h = Number(size[1] || 64) | 0;
      const sc = Number(img.scale || 1.0);
      const pos = img.position;
      const fn = (sc !== 1.0) ? 'imageScaled' : 'image';
      if (typeof pos === 'string') {
        if (sc !== 1.0) push(`    pg.${fn}(${JSON.stringify(img.path)}, ${JSON.stringify(pos)}, ${w}, ${h}, ${sc});`);
        else push(`    pg.${fn}(${JSON.stringify(img.path)}, ${JSON.stringify(pos)}, ${w}, ${h});`);
      } else if (Array.isArray(pos)) {
        const x = Number(pos[0] || 0) | 0;
        const y = Number(pos[1] || 0) | 0;
        if (sc !== 1.0) push(`    pg.${fn}(${JSON.stringify(img.path)}, ${x}, ${y}, ${w}, ${h}, ${sc});`);
        else push(`    pg.${fn}(${JSON.stringify(img.path)}, ${x}, ${y}, ${w}, ${h});`);
      } else {
        if (sc !== 1.0) push(`    pg.${fn}(${JSON.stringify(img.path)}, "TOP_CENTER", ${w}, ${h}, ${sc});`);
        else push(`    pg.${fn}(${JSON.stringify(img.path)}, "TOP_CENTER", ${w}, ${h});`);
      }
    }
    if (p.badge && typeof p.badge === "object") {
      const bd = p.badge;
      if (bd.text && typeof bd.text === "object") {
        const posStr = typeof bd.position === "string" ? bd.position : "BOTTOM_RIGHT";
        const bscale = Number(bd.text && bd.text.scale != null ? bd.text.scale : 0.7);
        const bls = Number(bd.text && bd.text.line_spacing != null ? bd.text.line_spacing : 0) | 0;
        push(`    pg.badge(${javaCompExpr(bd.text && bd.text.text ? bd.text.text : bd.text)}, ${bscale}f, ${bls}, ${javaStr(String(bd.background_color || "#CC000000"))}, ${Number(bd.radius||4)}, ${javaStr(posStr)});`);
      }
    }
    push('  })');
  });

  push('});');
  downloadText('auratip_tip.kubejs.js', lines.join('\n') + '\n', 'text/javascript;charset=utf-8');
}

function exportJavaSnippet() {
  function javaStr(s) {
    return JSON.stringify(String(s ?? ''));
  }

  function javaCompExpr(componentJson) {
    function fromJsonExpr(cleanObj) {
      const raw = JSON.stringify(cleanObj);
      if (!raw.includes('\"\"\"')) {
        return `Component.Serializer.fromJson(\"\"\"${raw}\"\"\")`;
      }
      return `Component.Serializer.fromJson(${javaStr(raw)})`;
    }

    function fmtConst(colorName) {
      if (!colorName || typeof colorName !== 'string') return null;
      const key = colorName.trim().toUpperCase().replace(/[^A-Z_]/g, '_');
      const allowed = new Set([
        'BLACK','DARK_BLUE','DARK_GREEN','DARK_AQUA','DARK_RED','DARK_PURPLE',
        'GOLD','GRAY','DARK_GRAY','BLUE','GREEN','AQUA','RED','LIGHT_PURPLE','YELLOW','WHITE'
      ]);
      return allowed.has(key) ? key : null;
    }

    if (componentJson === null || componentJson === undefined) return 'null';
    if (typeof componentJson === 'string') {
      return `Component.literal(${javaStr(componentJson)})`;
    }

    if (!isPlainObject(componentJson)) {
      return fromJsonExpr(componentJson);
    }

    const clean = sanitizeComponentJson(componentJson);

    if (Array.isArray(clean.extra) && clean.extra.length > 0) {
      return fromJsonExpr(clean);
    }

    const hasText = Object.prototype.hasOwnProperty.call(clean, 'text');
    const hasTranslate = Object.prototype.hasOwnProperty.call(clean, 'translate');

    let baseExpr = null;
    if (hasText) {
      baseExpr = `Component.literal(${javaStr(clean.text || '')})`;
    } else if (hasTranslate) {
      const args = Array.isArray(clean.with)
        ? clean.with.map(a => {
          if (a === null || a === undefined) return 'null';
          if (typeof a === 'number' || typeof a === 'boolean') return String(a);
          if (typeof a === 'string') return javaStr(a);
          return javaCompExpr(a);
        }).join(', ')
        : '';
      baseExpr = `Component.translatable(${javaStr(String(clean.translate || ''))}${args ? ', ' + args : ''})`;
    } else {
      return fromJsonExpr(clean);
    }

    const styleCalls = [];

    if (typeof clean.color === 'string' && clean.color.length > 0) {
      if (clean.color.startsWith('#') && (clean.color.length === 7 || clean.color.length === 9)) {
        const hex = clean.color.length === 9 ? clean.color.slice(3) : clean.color.slice(1);
        styleCalls.push(`withColor(TextColor.fromRgb(0x${hex.toUpperCase()}))`);
      } else {
        const fmt = fmtConst(clean.color);
        if (fmt) {
          styleCalls.push(`applyFormat(ChatFormatting.${fmt})`);
        } else {
          return fromJsonExpr(clean);
        }
      }
    }

    if (clean.bold) styleCalls.push('withBold(true)');
    if (clean.italic) styleCalls.push('withItalic(true)');
    if (clean.underlined) styleCalls.push('withUnderlined(true)');
    if (clean.strikethrough) styleCalls.push('withStrikethrough(true)');
    if (clean.obfuscated) styleCalls.push('withObfuscated(true)');

    if (styleCalls.length === 0) {
      return baseExpr;
    }

    return `${baseExpr}.withStyle(style -> style.${styleCalls.join('.')})`;
  }

  function javaRlExpr(s) {
    return `Identifier.parse(${javaStr(s)})`;
  }

  function javaMapExpr(obj) {
    if (!obj || typeof obj !== 'object') return 'Map.of()';
    const keys = Object.keys(obj);
    if (keys.length === 0) return 'Map.of()';
    if (keys.length > 8) {
      return `/* TODO: params map has ${keys.length} entries */ Map.of()`;
    }
    const pairs = [];
    for (const k of keys) {
      const v = obj[k];
      if (typeof v === 'number' || typeof v === 'boolean') pairs.push(`${javaStr(k)}, ${String(v)}`);
      else pairs.push(`${javaStr(k)}, ${javaStr(v)}`);
    }
    return `Map.of(${pairs.join(', ')})`;
  }

  const lines = [];
  const push = (s = '') => lines.push(s);

  if (mode === 'radial') {
    const m = radialState || {};
    const ms = m.menu_settings || {};
    const slots = Array.isArray(m.slots) ? m.slots : [];

    push('// AuraTip Radial Menu (Java)');
    push('// Register as runtime data via RadialMenuRegistry.setMenus(owner, menus).');
    push('');
    push('import cc.sighs.auratip.api.action.Actions;');
    push('import cc.sighs.auratip.api.radiamenu.RadialMenuBuilder;');
    push('import cc.sighs.auratip.api.radiamenu.RadialMenuRegistry;');
    push('import cc.sighs.auratip.data.RadialMenuData;');
    push('import cc.sighs.auratip.api.radiamenu.icon.ItemIcon;');
    push('import cc.sighs.auratip.api.radiamenu.icon.TextureIcon;');
    push('import cc.sighs.auratip.util.ItemStackUtil;');
    push('import net.minecraft.ChatFormatting;');
    push('import net.minecraft.core.RegistryAccess;');
    push('import net.minecraft.core.component.DataComponentPatch;');
    push('import net.minecraft.network.chat.Component;');
    push('import net.minecraft.network.chat.TextColor;');
    push('import net.minecraft.resources.ResourceLocation;');
    push('import net.minecraft.world.item.ItemStack;');
    push('import net.minecraft.world.item.ItemStackTemplate;');
    push('import java.util.List;');
    push('import java.util.Map;');
    push('');

    const id = String(m.id || 'auratip:menu');
    push(`RadialMenuData menu = new RadialMenuBuilder(${javaRlExpr(id)})`);
    push(`  .radii(${Number(ms.inner_radius ?? 55) | 0}, ${Number(ms.outer_radius ?? 100) | 0})`);
    push(`  .animationSpeed(${Number(ms.animation_speed ?? 1.0)})`);
    if (ms.ring_color) push(`  .ringColor(${javaStr(ms.ring_color)})`);
    if (ms.ring_colors) push(`  .ringColors(List.of(${(ms.ring_colors || []).map(javaStr).join(', ')}))`);
    if (ms.center_icon) push(`  .centerIcon(${javaRlExpr(ms.center_icon)})`);
    if (ms.close_key) push(`  .closeKey(${javaStr(ms.close_key)})`);

    slots.forEach(s => {
      const a = (s.action && typeof s.action === 'object') ? s.action : { type: 'auratip:run_command', command: '' };
      const type = String(a.type || 'auratip:run_command');
      const params = { ...a }; delete params.type;
      let actionExpr = '';
      if (type === 'auratip:run_command') actionExpr = `Actions.runCommand(${javaStr(params.command || '')})`;
      else if (type === 'auratip:simulate_key') actionExpr = `Actions.simulateKey(${Number(params.key_code || 0) | 0})`;
      else actionExpr = `Actions.script(${javaRlExpr(type)}, ${javaMapExpr(params)})`;

      const iconRaw = s.icon;
      let iconExpr;
      if (iconRaw && typeof iconRaw === 'object' && !Array.isArray(iconRaw)) {
        const tmpl = (iconRaw.stack && typeof iconRaw.stack === 'object') ? iconRaw.stack
                   : (iconRaw.stack  && typeof iconRaw.stack  === 'object') ? iconRaw.stack
                   : iconRaw;
        const itemId = String(tmpl.id || 'minecraft:air');
        const count = Number(tmpl.Count || 1) | 0;
        const icoScale = Number(iconRaw.scale || 1.0);
        const tmplJson = JSON.stringify({
          id: tmpl.id || 'minecraft:air',
          count: tmpl.Count || 1,
          components: tmpl.tag || {}
        });
        iconExpr = `new ItemIcon(ItemStackUtil.fromJson(${javaStr(tmplJson)}), ${icoScale}f)`;
      } else {
        const texId = String(iconRaw || 'minecraft:textures/item/paper.png');
        iconExpr = `new TextureIcon(${javaRlExpr(texId)})`;
      }

      push(`  .slot(${javaStr(String(s.name || 'Slot'))}, ${iconExpr}, ${actionExpr}, ${s.text ? javaCompExpr(s.text) : 'null'}, ${s.highlight_color ? javaStr(s.highlight_color) : 'null'})`);
    });

    push('  .build();');
    push('');
    push('RadialMenuRegistry.setMenus("<your_modid>", List.of(menu));');

    downloadText('auratip_radial_menu.java', lines.join('\n') + '\n', 'text/plain;charset=utf-8');
    return;
  }

  const tData = tipState || {};
  const trig = tData.trigger || {};
  const vs = tData.visual_settings || {};
  const beh = tData.behavior || {};
  const pages = Array.isArray(tData.pages) ? tData.pages : [];

  push('// AuraTip Tip (Java)');
  push('// Register as runtime data via TipRegistry.setTips(owner, tips).');
  push('');
  push('import cc.sighs.auratip.api.tip.TipBuilder;');
  push('import cc.sighs.auratip.api.tip.TipRegistry;');
  push('import cc.sighs.auratip.data.TipData;');
  push('import net.minecraft.ChatFormatting;');
  push('import net.minecraft.network.chat.Component;');
  push('import net.minecraft.network.chat.TextColor;');
  push('import net.minecraft.resources.ResourceLocation;');
  push('import java.util.List;');
  push('import java.util.Map;');
  push('');

  const id = String(tData.id || 'auratip:tip');
  const trigType = String(trig.type || 'auratip:first_join_world');
  const modeStr = String(trig.mode || 'once').toUpperCase();
  push(`TipData tip = new TipBuilder(${javaRlExpr(id)})`);
  push(`  .trigger(${javaRlExpr(trigType)}, TipData.Trigger.Mode.${modeStr}, ${Number(trig.cooldown || 0) | 0})`);

  push('  .visual(v -> {');
  if (vs.animation_style) push(`    v.animationStyle(${javaRlExpr(vs.animation_style)});`);
  if (vs.animation_speed != null) push(`    v.animationSpeed(${Number(vs.animation_speed)}f);`);
  if (vs.animation_params && typeof vs.animation_params === 'object' && Object.keys(vs.animation_params).length) push(`    v.animParams(${javaMapExpr(vs.animation_params)});`);
  if (vs.hover_animation_style) push(`    v.hoverAnimationStyle(${javaRlExpr(vs.hover_animation_style)});`);
  if (vs.hover_animation_speed != null) push(`    v.hoverAnimationSpeed(${Number(vs.hover_animation_speed)}f);`);
  if (vs.hover_only_on_hover != null) push(`    v.hoverOnlyOnHover(${!!vs.hover_only_on_hover});`);
  if (vs.hover_animation_params && typeof vs.hover_animation_params === 'object' && Object.keys(vs.hover_animation_params).length) push(`    v.hoverParams(${javaMapExpr(vs.hover_animation_params)});`);
  if (vs.stripe_width != null) push(`    v.stripeWidth(${Number(vs.stripe_width) | 0});`);
  if (vs.stripe_length_factor != null) push(`    v.stripeLengthFactor(${Number(vs.stripe_length_factor)}f);`);
  if (vs.theme_color) push(`    v.themeColor(${javaStr(vs.theme_color)});`);
  if (vs.width != null && vs.height != null) push(`    v.size(${Number(vs.width) | 0}, ${Number(vs.height) | 0});`);

  if (typeof vs.position === 'string') push(`    v.positionPreset(${javaStr(vs.position)});`);
  if (vs.layout && typeof vs.layout === "object") {
    const lay = vs.layout;
    if (lay.padding && typeof lay.padding === "object") {
      const p = lay.padding;
      push(`    v.padding(${Number(p.top||12)}, ${Number(p.right||12)}, ${Number(p.bottom||12)}, ${Number(p.left||12)});`);
    } else if (typeof lay.padding === "number") {
      push(`    v.padding(${lay.padding});`);
    }
    if (lay.element_spacing != null) push(`    v.elementSpacing(${Number(lay.element_spacing) | 0});`);
  }
  if (Array.isArray(vs.position)) push(`    v.positionAbsolute(${Number(vs.position[0] || 0) | 0}, ${Number(vs.position[1] || 0) | 0});`);

  if (vs.animation_from !== undefined) {
    if (typeof vs.animation_from === 'string') push(`    v.animationFromPreset(${javaStr(vs.animation_from)});`);
    if (Array.isArray(vs.animation_from)) push(`    v.animationFromAbsolute(${Number(vs.animation_from[0] || 0) | 0}, ${Number(vs.animation_from[1] || 0) | 0});`);
  }
  if (vs.animation_to !== undefined) {
    if (typeof vs.animation_to === 'string') push(`    v.animationToPreset(${javaStr(vs.animation_to)});`);
    if (Array.isArray(vs.animation_to)) push(`    v.animationToAbsolute(${Number(vs.animation_to[0] || 0) | 0}, ${Number(vs.animation_to[1] || 0) | 0});`);
  }

  if (vs.background && typeof vs.background === 'object') {
    const bg = vs.background;
    push(`    v.background(TipData.VisualSettings.BackgroundType.${String(bg.type || 'gradient').toUpperCase()}, List.of(${(bg.colors || []).map(javaStr).join(', ')}), ${Number(bg.border_radius || 8) | 0});`);
    if (bg.rounded != null) push(`    v.backgroundRounded(${!!bg.rounded});`);
    if (bg.image_path) push(`    v.backgroundImage(${javaStr(bg.image_path)});`);
    if (bg.shadow && typeof bg.shadow === "object") {
      const sh = bg.shadow;
      push(`    v.shadow(${!!sh.enabled}, ${javaStr(String(sh.color || "#8C000000"))}, ${Number(sh.offset_x||2)}, ${Number(sh.offset_y||2)}, ${Number(sh.size||4)});`);
    }
  }

  push('  })');

  push('  .behavior(b -> {');
  if (beh.default_duration != null) push(`    b.duration(${Number(beh.default_duration) | 0});`);
  if (beh.pause_timer_on_hover != null) push(`    b.pauseOnHover(${!!beh.pause_timer_on_hover});`);
  if (beh.closable_by_key) push(`    b.closeKey(${javaStr(beh.closable_by_key)});`);
  if (beh.allow_paging != null) push(`    b.allowPaging(${!!beh.allow_paging});`);
    if (beh.show_close_button != null) push(`    b.showCloseButton(${!!beh.show_close_button});`);
    if (beh.show_page_indicator != null) push(`    b.showPageIndicator(${!!beh.show_page_indicator});`);
  push('  })');

  pages.forEach(p => {
    const idx = Number(p.page_index || 0);
    push(`  .page(${idx}, pg -> {`);
    const title = p.title;
    const subtitle = p.subtitle;
    const content = p.content;
    if (title && title.text) push(`    pg.title(${javaCompExpr(title.text)}, ${Number(title.scale || 1.0)}f, ${Number(title.line_spacing || 0) | 0});`);
    if (title && title.divider) {
      const d = title.divider;
      push(`    pg.titleDivider(${Number(d.thickness || 1) | 0}, ${Number(d.margin_top || 4) | 0}, ${Number(d.margin_bottom || 4) | 0}, ${Number(d.length || 1.0)}f, ${javaStr(d.color || '')});`);
    }
    if (subtitle && subtitle.text) push(`    pg.subtitle(${javaCompExpr(subtitle.text)}, ${Number(subtitle.scale || 1.0)}f, ${Number(subtitle.line_spacing || 0) | 0});`);
    if (content && content.text) push(`    pg.content(${javaCompExpr(content.text)}, ${Number(content.scale || 1.0)}f, ${Number(content.line_spacing || 0) | 0});`);
    if (p.image && p.image.path) {
      const img = p.image;
      const size = Array.isArray(img.size) ? img.size : [64, 64];
      const w = Number(size[0] || 64) | 0;
      const h = Number(size[1] || 64) | 0;
      const sc = Number(img.scale || 1.0);
      const pos = img.position;
      if (typeof pos === 'string') {
        push(`    pg.imageScaled(${javaStr(img.path)}, ${javaStr(pos)}, ${w}, ${h}, ${sc}f);`);
      } else if (Array.isArray(pos)) {
        push(`    pg.imageScaled(${javaStr(img.path)}, ${Number(pos[0] || 0) | 0}, ${Number(pos[1] || 0) | 0}, ${w}, ${h}, ${sc}f);`);
      } else {
        push(`    pg.imageScaled(${javaStr(img.path)}, ${javaStr('TOP_CENTER')}, ${w}, ${h}, ${sc}f);`);
      }
    }
    if (p.badge && typeof p.badge === "object") {
      const bd = p.badge;
      if (bd.text && typeof bd.text === "object") {
        const posStr = typeof bd.position === "string" ? bd.position : "BOTTOM_RIGHT";
        push(`    pg.badge(${javaCompExpr(bd.text)}, ${javaStr(String(bd.background_color || "#CC000000"))}, ${Number(bd.radius||4)}, ${javaStr(posStr)});`);
      }
    }
    push('  })');
  });

  push('  .build();');
  push('');
  push('TipRegistry.setTips("<your_modid>", List.of(tip));');

  downloadText('auratip_tip.java', lines.join('\n') + '\n', 'text/plain;charset=utf-8');
}

// -----------------------------
// WebSocket connection
// -----------------------------

function connectWs() {
  setConn('wait', t('connecting'));
  const url = getWsUrl();
  ws = new WebSocket(url);

  ws.addEventListener('open', () => {
    setConn('ok', t('connected'));
    send({ type:'ping' });
  });

  ws.addEventListener('close', () => {
    setConn('bad', t('disconnected'));
    setTimeout(connectWs, 800);
  });

  ws.addEventListener('message', (ev) => {
    const msg = safeJsonParse(ev.data);
    if (!msg) return;
    if (msg.type === 'init') {
      initPayload = msg.payload || {};
      tipState = initPayload.defaultTip || null;
      radialState = initPayload.defaultRadialMenu || null;
      if (!animId.value) {
        animId.value = 'nekojs:editor_animation';
      }
      if (!animDefaults.value) {
        animDefaults.value = JSON.stringify({ offset: 32 }, null, 2);
      }
      if (!animTestParams.value) {
        animTestParams.value = JSON.stringify({ offset: 32 }, null, 2);
      }
      refreshAnimJavaSnippet(true);
      renderForm();
    }
    if (msg.type === 'animation_test_result') {
      if (msg.ok) animStatus.textContent = 'OK: ' + (msg.id || '');
      else animStatus.textContent = 'ERROR: ' + (msg.error || 'unknown');
    }
    if (msg.type === 'animation_apply_result') {
      if (msg.ok) animStatus.textContent = 'OK: ' + (msg.id || '');
      else animStatus.textContent = 'ERROR: ' + (msg.error || 'unknown');
    }
  });
}
