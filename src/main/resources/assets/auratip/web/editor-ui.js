// -----------------------------
// Schema UI (Codec-first)
// -----------------------------

function schemaDefaultValue(typeSchema, fieldSchema) {
  if (fieldSchema && fieldSchema.default !== undefined) return deepClone(fieldSchema.default);
  if (!typeSchema || typeof typeSchema !== 'object') return null;
  const k = typeSchema.kind;
  if (k === 'string' || k === 'resource_location') return '';
  if (k === 'number') return 0;
  if (k === 'boolean') return false;
  if (k === 'enum') return (typeSchema.options && typeSchema.options[0]) ? typeSchema.options[0] : '';
  if (k === 'fixed_int_array2') return [0, 0];
  if (k === 'map_dynamic') return {};
  if (k === 'component') return { text: '' };
  if (k === 'either') {
    // default to the first variant's type
    const v0 = typeSchema.variants && typeSchema.variants[0] ? typeSchema.variants[0].type : null;
    return schemaDefaultValue(v0, null);
  }
  if (k === 'action') return { type: 'auratip:run_command', command: '/say hello' };
  if (k === 'array') return [];
  if (k === 'object') {
    const obj = {};
    const fields = Array.isArray(typeSchema.fields) ? typeSchema.fields : [];
    fields.forEach(f => {
      if (!f || !f.name) return;
      if (f.optional) return;
      obj[f.name] = schemaDefaultValue(f.type, f);
    });
    return obj;
  }
  return null;
}

function renderSchema(rootObj, schema, rootPath) {
  if (!rootObj || typeof rootObj !== 'object') {
    formRoot.appendChild(el('div', 'text-white/40 text-sm', t('no_editable')));
    return;
  }

  const title = (rootPath && rootPath[0] === 'radial') ? t('schema_title_radial') : t('schema_title_tip');
  const sec = section(title);
  sec.appendChild(el('div', 'text-[11px] text-white/25', t('schema_hint')));
  sec.appendChild(renderSchemaObject(rootObj, schema, rootPath, 0));
  formRoot.appendChild(sec);
}

function fieldLabel(fieldSchema) {
  if (!fieldSchema) return '';
  const name = fieldSchema.name || '';
  const labelKey = fieldSchema.label_key;
  const label = labelKey ? t(labelKey) : name;
  if (label && label !== name) return `${label} (${name})`;
  return name;
}

function fieldDesc(fieldSchema) {
  if (!fieldSchema) return '';
  const key = fieldSchema.desc_key;
  if (!key) return '';
  const v = t(key);
  return (v && v !== key) ? v : '';
}

function rowField(labelText, control, fieldSchema, onRemove) {
  const r = el('div', 'space-y-1');
  const head = el('div', 'flex items-center justify-between');
  head.appendChild(el('div', 'text-[11px] text-white/50', labelText));

  if (fieldSchema && fieldSchema.optional && typeof onRemove === 'function') {
    const rm = el('button', 'text-[11px] px-2 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-white/50 transition', t('remove'));
    rm.addEventListener('click', onRemove);
    head.appendChild(rm);
  }

  r.appendChild(head);
  r.appendChild(control);
  const desc = fieldDesc(fieldSchema);
  if (desc) {
    r.appendChild(el('div', 'text-[11px] text-white/25', desc));
  }
  return r;
}

function renderSchemaObject(obj, schema, path, depth) {
  const wrap = el('div', 'space-y-3');
  const fields = schema && Array.isArray(schema.fields) ? schema.fields : [];

  // known fields (in codec order)
  fields.forEach(f => {
    const k = f.name;
    const nextPath = (path || []).concat([k]);
    const has = Object.prototype.hasOwnProperty.call(obj, k);

    if (!has) {
      if (f.optional) {
        const addBtn = el('button', 'w-full text-xs bg-white/10 hover:bg-white/20 px-4 py-2 rounded-xl transition', t('add'));
        addBtn.addEventListener('click', () => {
          obj[k] = schemaDefaultValue(f.type, f);
          renderForm();
          sendLiveDebounced();
        });
        wrap.appendChild(rowField(fieldLabel(f), addBtn, f, null));
        return;
      }
      obj[k] = schemaDefaultValue(f.type, f);
    }

    const value = obj[k];

    // special cases (animations/action/position)
    if (endsWithPath(nextPath, ['visual_settings', 'animation_style'])) {
      wrap.appendChild(rowField(fieldLabel(f), renderAnimationStyleSelect(obj, false), f, f.optional ? () => { delete obj[k]; renderForm(); sendLiveDebounced(); } : null));
      return;
    }
    if (endsWithPath(nextPath, ['visual_settings', 'hover_animation_style'])) {
      wrap.appendChild(rowField(fieldLabel(f), renderAnimationStyleSelect(obj, true), f, f.optional ? () => { delete obj[k]; renderForm(); sendLiveDebounced(); } : null));
      return;
    }
    if (endsWithPath(nextPath, ['visual_settings', 'animation_params'])) {
      wrap.appendChild(renderAnimParamsBox(obj, false, f.optional, f.optional ? () => { delete obj[k]; renderForm(); sendLiveDebounced(); } : null));
      return;
    }
    if (endsWithPath(nextPath, ['visual_settings', 'hover_animation_params'])) {
      wrap.appendChild(renderAnimParamsBox(obj, true, f.optional, f.optional ? () => { delete obj[k]; renderForm(); sendLiveDebounced(); } : null));
      return;
    }
    if (endsWithPath(nextPath, ['visual_settings', 'position'])) {
      wrap.appendChild(rowField(fieldLabel(f), buildPositionControl(obj, k), f, f.optional ? () => { delete obj[k]; renderForm(); sendLiveDebounced(); } : null));
      return;
    }
    if (f.type && f.type.kind === 'action') {
      wrap.appendChild(renderActionEditor(obj, k));
      return;
    }

    wrap.appendChild(renderSchemaField(obj, f, value, nextPath, depth));
  });

  // extra fields (not in schema)
  Object.keys(obj || {}).forEach(k => {
    if (fields.some(f => f && f.name === k)) return;
    const v = obj[k];
    const nextPath = (path || []).concat([k]);
    wrap.appendChild(renderAutoField(obj, k, v, nextPath, depth));
  });

  return wrap;
}

function renderSchemaField(parent, fieldSchema, value, path, depth) {
  const k = fieldSchema.name;
  const type = fieldSchema.type || {};
  const kind = type.kind || 'unknown';

  const removeFn = fieldSchema.optional ? () => { delete parent[k]; renderForm(); sendLiveDebounced(); } : null;

  if (kind === 'object') {
    const details = el('details', 'glass rounded-2xl p-3 bg-black/20 border border-white/10');
    details.open = depth < 6;
    const summary = el('summary', 'cursor-pointer text-xs font-semibold text-white/70 select-none flex items-center justify-between');
    summary.appendChild(el('span', '', fieldLabel(fieldSchema) || k));
    if (removeFn) {
      const rm = el('button', 'text-[11px] px-2 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-white/50 transition', t('remove'));
      rm.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        removeFn();
      });
      summary.appendChild(rm);
    }
    details.appendChild(summary);

    const inner = el('div', 'mt-3 space-y-3');
    const desc = fieldDesc(fieldSchema);
    if (desc) inner.appendChild(el('div', 'text-[11px] text-white/25', desc));
    inner.appendChild(renderSchemaObject(parent[k], type, path, depth + 1));

    const openRaw = el('button', 'w-full text-xs bg-white/10 hover:bg-white/20 px-4 py-2 rounded-xl transition', t('open_json_inspector'));
    openRaw.addEventListener('click', () => {
      animEditor.classList.add('translate-x-[120%]');
      jsonInspector.classList.remove('translate-x-[120%]');
      refreshJsonArea(true);
    });
    inner.appendChild(openRaw);

    details.appendChild(inner);
    return details;
  }

  if (kind === 'array') {
    return rowField(fieldLabel(fieldSchema), renderSchemaArray(parent, k, value, type, path, depth + 1), fieldSchema, removeFn);
  }

  if (kind === 'enum') {
    return rowField(fieldLabel(fieldSchema), renderEnumSelect(value, type.options || [], nv => { parent[k] = nv; sendLiveDebounced(); }), fieldSchema, removeFn);
  }

  if (kind === 'either') {
    return rowField(fieldLabel(fieldSchema), renderEither(parent, k, value, type, path), fieldSchema, removeFn);
  }

  if (kind === 'fixed_int_array2') {
    return rowField(fieldLabel(fieldSchema), renderFixedIntArray2(parent, k, value), fieldSchema, removeFn);
  }

  if (kind === 'map_dynamic' && path && path.length >= 2 && path[path.length-2] === 'template' && path[path.length-1] === 'components') {
    const compList = initPayload.dataComponentTypes || [];
    return rowField(fieldLabel(fieldSchema), renderDataComponentSelector(parent, k, compList), fieldSchema, removeFn);
  }
  if (kind === 'map_dynamic') {
    return rowField(fieldLabel(fieldSchema), inputJson(value, v => { parent[k] = v || {}; sendLiveDebounced(); }, 8), fieldSchema, removeFn);
  }

  if (kind === 'component') {
    return rowField(fieldLabel(fieldSchema), renderComponentEditor(parent, k, value), fieldSchema, removeFn);
  }

  // primitives + fallback
  if (kind === 'number') {
    return rowField(fieldLabel(fieldSchema), inputNumber(value, v => { parent[k] = v; sendLiveDebounced(); }), fieldSchema, removeFn);
  }
  if (kind === 'boolean') {
    return rowField(fieldLabel(fieldSchema), inputBool(value, v => { parent[k] = !!v; sendLiveDebounced(); }), fieldSchema, removeFn);
  }

  // string/resource_location/unknown
  {
    const colorMeta = isSchemaColorPath(path);
    if (colorMeta && kind === 'string') {
      return rowField(fieldLabel(fieldSchema), colorPalette(value ?? '', v => { parent[k] = v; sendLiveDebounced(); }, { allowAlpha: !!colorMeta.allowAlpha }), fieldSchema, removeFn);
    }
    return rowField(fieldLabel(fieldSchema), inputText(value ?? '', v => { parent[k] = v; sendLiveDebounced(); }), fieldSchema, removeFn);
  }
}

function renderSchemaArray(parent, key, arr, typeSchema, path, depth) {
  const wrap = el('div', 'space-y-2');
  if (!Array.isArray(arr)) {
    parent[key] = [];
    arr = parent[key];
  }

  arr.forEach((item, idx) => {
    const card = el('div', 'p-3 rounded-2xl bg-black/20 border border-white/10 space-y-2');
    const head = el('div', 'flex items-center justify-between');
    head.appendChild(el('div', 'text-[11px] text-white/50', `#${idx}`));
    const del = el('button', 'text-xs bg-rose-600/30 hover:bg-rose-600/50 px-3 py-1.5 rounded-lg transition', t('delete'));
    del.addEventListener('click', () => {
      arr.splice(idx, 1);
      renderForm();
      sendLiveDebounced();
    });
    head.appendChild(del);
    card.appendChild(head);

    const itemSchema = typeSchema.item;
    const itemPath = (path || []).concat([idx]);

    if (itemSchema && itemSchema.kind === 'object') {
      if (!item || typeof item !== 'object' || Array.isArray(item)) {
        arr[idx] = schemaDefaultValue(itemSchema, null);
      }
      card.appendChild(renderSchemaObject(arr[idx], itemSchema, itemPath, depth + 1));
    } else if (itemSchema && itemSchema.kind === 'either') {
      card.appendChild(renderEither({ tmp: arr }, idx, item, itemSchema, itemPath));
    } else {
      // fallback to auto inference
      if (Array.isArray(item)) {
        card.appendChild(renderAutoArray({ tmp: item }, 'tmp', item, itemPath, depth + 1));
      } else if (item && typeof item === 'object') {
        card.appendChild(renderAutoObject(item, itemPath, depth + 1));
        card.appendChild(row(t('raw_json'), inputJson(item, v => { arr[idx] = v; sendLiveDebounced(); }, 8)));
      } else if (typeof item === 'number') {
        card.appendChild(row('value', inputNumber(item, v => { arr[idx] = v; sendLiveDebounced(); })));
      } else if (typeof item === 'boolean') {
        card.appendChild(row('value', inputBool(item, v => { arr[idx] = !!v; sendLiveDebounced(); })));
      } else {
        const meta = isSchemaColorPath(itemPath.slice(0, -1));
        if (meta && meta.arrayItem) {
          card.appendChild(row('value', colorPalette(item ?? '', v => { arr[idx] = v; sendLiveDebounced(); }, { allowAlpha: !!meta.allowAlpha })));
        } else {
          card.appendChild(row('value', inputText(item ?? '', v => { arr[idx] = v; sendLiveDebounced(); })));
        }
      }
    }

    wrap.appendChild(card);
  });

  const addBtn = el('button', 'text-xs bg-white/10 hover:bg-white/20 px-4 py-2 rounded-xl transition', t('add_item'));
  addBtn.addEventListener('click', () => {
    const itemSchema = typeSchema.item;
    const item = schemaDefaultValue(itemSchema, null);
    if (key === 'pages' && item && typeof item === 'object' && item.page_index !== undefined) {
      item.page_index = arr.length;
    }
    arr.push(item);
    renderForm();
    sendLiveDebounced();
  });
  wrap.appendChild(addBtn);
  return wrap;
}

function renderFixedIntArray2(parent, key, value) {
  if (!Array.isArray(value) || value.length < 2) {
    parent[key] = [0, 0];
    value = parent[key];
  }
  const wrap = el('div', 'grid grid-cols-2 gap-2');
  wrap.appendChild(inputNumber(value[0] || 0, v => { value[0] = v|0; sendLiveDebounced(); }));
  wrap.appendChild(inputNumber(value[1] || 0, v => { value[1] = v|0; sendLiveDebounced(); }));
  return wrap;
}

function renderEither(parent, key, value, typeSchema, path) {
  const variants = Array.isArray(typeSchema.variants) ? typeSchema.variants : [];

  function detectVariant(val) {
    if (variants.length > 1) {
      const v1 = variants[1];
      if (v1 && v1.type) {
        if (v1.type.kind === 'fixed_int_array2' && Array.isArray(val)) return 1;
        if (v1.type.kind === 'object' && val && typeof val === 'object' && !Array.isArray(val)) {
          const v1Fields = v1.type.fields || [];
          for (const f of v1Fields) {
            if (f && f.name && (f.name in val)) return 1;
          }
        }
      }
    }
    return 0;
  }

  const sel = el('select', 'w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  variants.forEach((v, idx) => {
    const o = el('option', '', v.label || String(idx));
    o.value = String(idx);
    sel.appendChild(o);
  });

  sel.value = String(detectVariant(value));

  const body = el('div', 'space-y-2');

  function renderBody() {
    body.innerHTML = '';
    const idx = Number(sel.value || 0);
    const variant = variants[idx] || variants[0];
    const vt = variant ? variant.type : null;
    if (idx === 0) {
      if (vt && vt.kind === 'enum') {
        body.appendChild(renderEnumSelect(parent[key] ?? (vt.options ? vt.options[0] : ''), vt.options || [], v => { parent[key] = v; sendLiveDebounced(); }));
      } else {
        if (typeof parent[key] !== 'string') parent[key] = schemaDefaultValue(vt, null);
        body.appendChild(inputText(parent[key] ?? '', v => { parent[key] = v; sendLiveDebounced(); }));
      }
    } else if (vt && vt.kind === 'object') {
      // Variant 1: object — render fields from schema
      const obj = (parent[key] && typeof parent[key] === 'object' && !Array.isArray(parent[key]))
        ? parent[key] : (parent[key] = schemaDefaultValue(vt, null));
      // Inject type field if variant has a "type" enum field
      const typeField = (vt.fields || []).find(f => f && f.name === 'type' && f.type && f.type.kind === 'enum');
      if (typeField && typeField.type && typeField.type.options && typeField.type.options.length > 0) {
        obj.type = typeField.type.options[0];
      }
      const fields = Array.isArray(vt.fields) ? vt.fields : [];
      fields.forEach(f => {
        if (!f || !f.name) return;
        if (!(f.name in obj)) obj[f.name] = schemaDefaultValue(f.type, f);
        body.appendChild(renderSchemaField(obj, f, obj[f.name], path.concat(key, f.name), () => {
          if (f.optional) { delete obj[f.name]; renderBody(); sendLiveDebounced(); }
        }));
      });
    } else {
      // Fallback: fixed int array (position)
      if (!Array.isArray(parent[key])) parent[key] = [0, 0];
      body.appendChild(renderFixedIntArray2(parent, key, parent[key]));
    }
  }

  sel.addEventListener('change', () => {
    const idx = Number(sel.value || 0);
    const variant = variants[idx] || variants[0];
    parent[key] = schemaDefaultValue(variant ? variant.type : null, null);
    // Inject type field if variant has a "type" enum field
    if (variant && variant.type && variant.type.fields) {
      const typeField = variant.type.fields.find(f => f && f.name === 'type' && f.type && f.type.kind === 'enum');
      if (typeField && typeField.type && typeField.type.options && typeField.type.options.length > 0) {
        if (typeof parent[key] !== 'object') parent[key] = {};
        parent[key].type = typeField.type.options[0];
      }
    }
    renderBody();
    sendLiveDebounced();
  });

  renderBody();

  const wrap = el('div', 'space-y-2');
  wrap.appendChild(sel);
  wrap.appendChild(body);
  return wrap;
}

// -----------------------------
// Component editor (basic Style fields)
// -----------------------------

function renderComponentEditor(parent, key, value) {
  parent[key] = ensureComponentValue(value);
  const c = parent[key];

  const card = el('div', 'p-3 rounded-2xl bg-black/20 border border-white/10 space-y-3');

  // Content (best-effort)
  const hasText = Object.prototype.hasOwnProperty.call(c, 'text');
  const hasTranslate = Object.prototype.hasOwnProperty.call(c, 'translate');
  const kind = hasTranslate ? 'translate' : (hasText ? 'text' : 'custom');
  card.appendChild(el('div', 'text-[11px] text-white/40', `${t('component.kind')}: ${kind}`));

  if (hasText || kind === 'custom') {
    card.appendChild(row(t('component.text'), inputText(c.text ?? '', v => { c.text = v; sendLiveDebounced(); }), t('component.text.desc')));
  }
  if (hasTranslate) {
    card.appendChild(row(t('component.translate'), stringWithUnset(c, 'translate', 'my.translation.key'), t('component.translate.desc')));
  }

  // Style fields
  card.appendChild(row(t('style.color'), colorPalette(c.color ?? '', v => { c.color = v ? (v.length > 7 ? '#' + v.slice(-6) : v) : undefined; sendLiveDebounced(); }, { allowAlpha: true }), t('style.color.desc')));
  card.appendChild(row(t('style.bold'), boolWithUnset(c, 'bold'), t('style.bold.desc')));
  card.appendChild(row(t('style.italic'), boolWithUnset(c, 'italic'), t('style.italic.desc')));
  card.appendChild(row(t('style.underlined'), boolWithUnset(c, 'underlined'), t('style.underlined.desc')));
  card.appendChild(row(t('style.strikethrough'), boolWithUnset(c, 'strikethrough'), t('style.strikethrough.desc')));
  card.appendChild(row(t('style.obfuscated'), boolWithUnset(c, 'obfuscated'), t('style.obfuscated.desc')));

  // Raw editing is centralized in the JSON inspector.
  const openRaw = el('button', 'w-full text-xs bg-white/10 hover:bg-white/20 px-4 py-2 rounded-xl transition', t('component.open_json'));
  openRaw.addEventListener('click', () => {
    animEditor.classList.add('translate-x-[120%]');
    jsonInspector.classList.remove('translate-x-[120%]');
    refreshJsonArea(true);
  });
  card.appendChild(openRaw);

  return card;
}

// -----------------------------
// Auto UI (type inference)
// -----------------------------

function renderAuto(rootObj, rootPath) {
  if (!rootObj || typeof rootObj !== 'object') {
    formRoot.appendChild(el('div', 'text-white/40 text-sm', t('no_editable')));
    return;
  }

  const title = (rootPath && rootPath[0] === 'radial') ? 'RadialMenuData' : 'TipData';
  const sec = section(title + ' (auto)');
  sec.appendChild(el('div', 'text-[11px] text-white/25', 'Type inference fallback (no schema).'));
  sec.appendChild(renderAutoObject(rootObj, rootPath, 0));
  formRoot.appendChild(sec);
}

function renderAutoObject(obj, path, depth) {
  const wrap = el('div', 'space-y-3');
  const keys = Object.keys(obj).sort();
  keys.forEach(k => {
    const v = obj[k];
    const nextPath = (path || []).concat([k]);

    // VisualSettings special cases (animation style + params)
    if (endsWithPath(nextPath, ['visual_settings', 'animation_style'])) {
      wrap.appendChild(row(k, renderAnimationStyleSelect(obj, false), '下拉菜单读取所有已注册 transition 动画'));
      return;
    }
    if (endsWithPath(nextPath, ['visual_settings', 'hover_animation_style'])) {
      wrap.appendChild(row(k, renderAnimationStyleSelect(obj, true), '下拉菜单读取所有已注册 hover 动画'));
      return;
    }
    if (endsWithPath(nextPath, ['visual_settings', 'animation_params'])) {
      wrap.appendChild(renderAnimParamsBox(obj, false));
      return;
    }
    if (endsWithPath(nextPath, ['visual_settings', 'hover_animation_params'])) {
      wrap.appendChild(renderAnimParamsBox(obj, true));
      return;
    }
    if (endsWithPath(nextPath, ['visual_settings', 'position'])) {
      wrap.appendChild(renderPositionControl(obj, 'position'));
      return;
    }

    // Trigger mode enum
    if (endsWithPath(nextPath, ['trigger', 'mode'])) {
      wrap.appendChild(row(k, renderEnumSelect(
        v ?? 'repeatable',
        ['once', 'repeatable'],
        nv => { obj[k] = nv; sendLiveDebounced(); }
      )));
      return;
    }

    // Background type enum
    if (endsWithPath(nextPath, ['background', 'type'])) {
      wrap.appendChild(row(k, renderEnumSelect(
        v ?? 'gradient',
        ['gradient', 'solid', 'image'],
        nv => { obj[k] = nv; sendLiveDebounced(); }
      )));
      return;
    }

    // Radial slot action editor
    if (k === 'action' && v && typeof v === 'object' && endsWithPath(nextPath, ['slots', 'action'])) {
      wrap.appendChild(renderActionEditor(obj, k));
      return;
    }

    wrap.appendChild(renderAutoField(obj, k, v, nextPath, depth));
  });
  return wrap;
}

function renderAutoField(parent, key, value, path, depth) {
  if (Array.isArray(value)) {
    return row(key, renderAutoArray(parent, key, value, path, depth + 1));
  }
  if (value && typeof value === 'object') {
    // Component JSON or any object: use nested editor, with JSON fallback toggle
    const details = el('details', 'glass rounded-2xl p-3 bg-black/20 border border-white/10');
    details.open = depth < 6;
    const summary = el('summary', 'cursor-pointer text-xs font-semibold text-white/70 select-none');
    summary.textContent = key;
    details.appendChild(summary);

    const inner = el('div', 'mt-3 space-y-3');
    inner.appendChild(renderAutoObject(value, path, depth + 1));

    const openRaw = el('button', 'w-full text-xs bg-white/10 hover:bg-white/20 px-4 py-2 rounded-xl transition', t('open_json_inspector'));
    openRaw.addEventListener('click', () => {
      animEditor.classList.add('translate-x-[120%]');
      jsonInspector.classList.remove('translate-x-[120%]');
      refreshJsonArea(true);
    });
    inner.appendChild(openRaw);
    details.appendChild(inner);
    return details;
  }

  // primitive inference
  if (typeof value === 'number') {
    return row(key, inputNumber(value, v => { parent[key] = v; sendLiveDebounced(); }));
  }
  if (typeof value === 'boolean') {
    return row(key, inputBool(value, v => { parent[key] = !!v; sendLiveDebounced(); }));
  }
  // string / null
  return row(key, inputText(value ?? '', v => {
    if (v === '' && (value === null || value === undefined)) {
      parent[key] = '';
    } else {
      parent[key] = v;
    }
    sendLiveDebounced();
  }));
}

function renderAutoArray(parent, key, arr, path, depth) {
  const wrap = el('div', 'space-y-2');
  if (!Array.isArray(arr)) {
    parent[key] = [];
    arr = parent[key];
  }

  arr.forEach((item, idx) => {
    const card = el('div', 'p-3 rounded-2xl bg-black/20 border border-white/10 space-y-2');
    const head = el('div', 'flex items-center justify-between');
    head.appendChild(el('div', 'text-[11px] text-white/50', `#${idx}`));
    const del = el('button', 'text-xs bg-rose-600/30 hover:bg-rose-600/50 px-3 py-1.5 rounded-lg transition', t('delete'));
    del.addEventListener('click', () => {
      arr.splice(idx, 1);
      renderForm();
      sendLiveDebounced();
    });
    head.appendChild(del);
    card.appendChild(head);

    const itemPath = (path || []).concat([idx]);
    // Render array item:
    if (Array.isArray(item)) {
      card.appendChild(renderAutoArray({ tmp: item }, 'tmp', item, itemPath, depth + 1));
    } else if (item && typeof item === 'object') {
      card.appendChild(renderAutoObject(item, itemPath, depth + 1));
    } else if (typeof item === 'number') {
      card.appendChild(row('value', inputNumber(item, v => { arr[idx] = v; sendLiveDebounced(); })));
    } else if (typeof item === 'boolean') {
      card.appendChild(row('value', inputBool(item, v => { arr[idx] = !!v; sendLiveDebounced(); })));
    } else {
      card.appendChild(row('value', inputText(item ?? '', v => { arr[idx] = v; sendLiveDebounced(); })));
    }

    wrap.appendChild(card);
  });

  const addBtn = el('button', 'text-xs bg-white/10 hover:bg-white/20 px-4 py-2 rounded-xl transition', t('add_item'));
  addBtn.addEventListener('click', () => {
    arr.push(null);
    renderForm();
    sendLiveDebounced();
  });
  wrap.appendChild(addBtn);
  return wrap;
}

// -----------------------------
// Shared UI controls
// -----------------------------

function renderEnumSelect(value, options, onChange) {
  const sel = el('select', 'w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  (options || []).forEach(opt => {
    const labelKey = `enum.${opt}`;
    const label = t(labelKey);
    const o = el('option', '', (label && label !== labelKey) ? label : opt);
    o.value = opt;
    sel.appendChild(o);
  });
  sel.value = value ?? (options && options[0] ? options[0] : '');
  sel.addEventListener('change', () => onChange(sel.value));
  return sel;
}

function renderAnimationStyleSelect(visualSettings, hover) {
  const sel = el('select', 'w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  const list = hover ? (initPayload.hoverAnimations || []) : (initPayload.transitionAnimations || []);
  list.forEach(id => {
    const o = el('option', '', id);
    o.value = id;
    sel.appendChild(o);
  });

  const key = hover ? 'hover_animation_style' : 'animation_style';
  sel.value = visualSettings[key] ?? (hover ? 'auratip:none' : 'auratip:fade_and_slide');
  sel.addEventListener('change', () => {
    visualSettings[key] = sel.value;
    rebuildAnimParamsInto(visualSettings, hover);
    renderForm();
    sendLiveDebounced();
  });
  return sel;
}

function renderActionEditor(slotObj, actionKey) {
  const card = el('div', 'p-3 rounded-2xl bg-black/20 border border-white/10 space-y-3');
  card.appendChild(el('div', 'text-xs font-semibold text-white/70', t('radial.slot.action')));

  slotObj[actionKey] = (slotObj[actionKey] && typeof slotObj[actionKey] === 'object') ? slotObj[actionKey] : { type: 'auratip:run_command' };
  const action = slotObj[actionKey];

  const actSel = el('select', 'w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  (initPayload.actionTypes || []).forEach(t => {
    const o = el('option', '', t);
    o.value = t;
    actSel.appendChild(o);
  });
  actSel.value = action.type || 'auratip:run_command';
  actSel.addEventListener('change', () => {
    const nextType = actSel.value;
    const defs = actionDefs(nextType);
    const next = { type: nextType };
    if (Array.isArray(defs)) {
      defs.forEach(d => { if (d && d.name) next[d.name] = (d.default !== undefined) ? d.default : ''; });
    }
    slotObj[actionKey] = next;
    renderForm();
    sendLiveDebounced();
  });
  card.appendChild(row('type', actSel));

  const defs = actionDefs(slotObj[actionKey].type || '');
  if (Array.isArray(defs)) {
    defs.forEach(d => {
      const name = d.name;
      const kind = d.kind || 'string';
      const cur = (slotObj[actionKey][name] !== undefined) ? slotObj[actionKey][name] : (d.default !== undefined ? d.default : '');
      const control = (kind === 'number')
              ? inputNumber(cur, v => { slotObj[actionKey][name] = v; sendLiveDebounced(); })
              : inputText(cur, v => { slotObj[actionKey][name] = v; sendLiveDebounced(); });
      card.appendChild(row(name, control));
    });
  } else {
    const openRaw = el('button', 'w-full text-xs bg-white/10 hover:bg-white/20 px-4 py-2 rounded-xl transition', t('open_json_inspector'));
    openRaw.addEventListener('click', () => {
      animEditor.classList.add('translate-x-[120%]');
      jsonInspector.classList.remove('translate-x-[120%]');
      refreshJsonArea(true);
    });
    card.appendChild(openRaw);
  }

  return card;
}

// -----------------------------
// Form rendering
// -----------------------------

function renderForm() {
  formRoot.innerHTML = '';
  if (!initPayload) {
    formRoot.appendChild(el('div','text-white/40 text-sm', t('waiting_init')));
    return;
  }

  const schema = getSchemaForMode();
  if (mode === 'radial') {
    if (!radialState) radialState = initPayload.defaultRadialMenu;
    currentId.textContent = 'id: ' + (radialState && radialState.id ? radialState.id : '-');
    if (schema) renderSchema(radialState, schema, ['radial']);
    else renderAuto(radialState, ['radial']);
  } else {
    if (!tipState) tipState = initPayload.defaultTip;
    currentId.textContent = 'id: ' + (tipState && tipState.id ? tipState.id : '-');
    if (schema) renderSchema(tipState, schema, ['tip']);
    else renderAuto(tipState, ['tip']);
  }

  // keep JSON inspector in sync (unless user is typing)
  refreshJsonArea(false);
}

function getSchemaForMode() {
  const schemas = (initPayload && initPayload.schemas) ? initPayload.schemas : null;
  if (!schemas) return null;
  return (mode === 'radial') ? schemas.radial : schemas.tip;
}

function renderTipForm() {
  const tip = tipState; if (!tip) return;
  const trigger = ensureObj(tip, 'trigger', {});
  const visual = ensureObj(tip, 'visual_settings', {});
  const behavior = ensureObj(tip, 'behavior', {});
  const pages = ensureArr(tip, 'pages', []);

  const secTrigger = section('Trigger');
  secTrigger.appendChild(row('type', inputText(trigger.type ?? 'auratip:editor', v => { trigger.type=v; sendLiveDebounced(); }), 'ResourceLocation string'));
  const modeSel = el('select','w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  ['once','repeatable'].forEach(m => { const o=el('option','',m); o.value=m; modeSel.appendChild(o); });
  modeSel.value = trigger.mode ?? 'repeatable';
  modeSel.addEventListener('change', () => { trigger.mode=modeSel.value; sendLiveDebounced(); });
  secTrigger.appendChild(row('mode', modeSel));
  secTrigger.appendChild(row('cooldown', inputNumber(trigger.cooldown ?? 0, v => { trigger.cooldown=Math.max(0, v|0); sendLiveDebounced(); })));
  formRoot.appendChild(secTrigger);

  const secVisual = section('Visual Settings');
  const animSel = el('select','w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  (initPayload.transitionAnimations || []).forEach(id => { const o=el('option','',id); o.value=id; animSel.appendChild(o); });
  animSel.value = visual.animation_style ?? 'auratip:fade_and_slide';
  animSel.addEventListener('change', () => { visual.animation_style=animSel.value; rebuildAnimParamsInto(visual,false); renderForm(); sendLiveDebounced(); });
  secVisual.appendChild(row('animation_style', animSel));
  secVisual.appendChild(row('animation_speed', inputNumber(visual.animation_speed ?? 1.0, v => { visual.animation_speed=v; sendLiveDebounced(); })));
  secVisual.appendChild(renderAnimParamsBox(visual,false));

  const hoverSel = el('select','w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  (initPayload.hoverAnimations || []).forEach(id => { const o=el('option','',id); o.value=id; hoverSel.appendChild(o); });
  hoverSel.value = visual.hover_animation_style ?? 'auratip:none';
  hoverSel.addEventListener('change', () => { visual.hover_animation_style=hoverSel.value; rebuildAnimParamsInto(visual,true); renderForm(); sendLiveDebounced(); });
  secVisual.appendChild(row('hover_animation_style', hoverSel));
  secVisual.appendChild(row('hover_animation_speed', inputNumber(visual.hover_animation_speed ?? 1.0, v => { visual.hover_animation_speed=v; sendLiveDebounced(); })));
  secVisual.appendChild(row('hover_only_on_hover', inputBool(visual.hover_only_on_hover ?? false, v => { visual.hover_only_on_hover=!!v; sendLiveDebounced(); })));
  secVisual.appendChild(renderAnimParamsBox(visual,true));
  secVisual.appendChild(row('stripe_width', inputNumber(visual.stripe_width ?? 1, v => { visual.stripe_width=v|0; sendLiveDebounced(); })));
  secVisual.appendChild(row('stripe_length_factor', inputNumber(visual.stripe_length_factor ?? 1.0, v => { visual.stripe_length_factor=v; sendLiveDebounced(); })));
  secVisual.appendChild(row('theme_color', inputText(visual.theme_color ?? '#CC4FC3F7', v => { visual.theme_color=v; sendLiveDebounced(); })));
  secVisual.appendChild(row('width', inputNumber(visual.width ?? 280, v => { visual.width=v|0; sendLiveDebounced(); })));
  secVisual.appendChild(row('height', inputNumber(visual.height ?? 180, v => { visual.height=v|0; sendLiveDebounced(); })));
  secVisual.appendChild(renderPositionControl(visual,'position'));

  const bg = ensureObj(visual,'background',{});
  const bgTypeSel = el('select','w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  ['gradient','solid','image'].forEach(t => { const o=el('option','',t); o.value=t; bgTypeSel.appendChild(o); });
  bgTypeSel.value = bg.type ?? 'gradient';
  bgTypeSel.addEventListener('change', () => { bg.type=bgTypeSel.value; sendLiveDebounced(); });
  secVisual.appendChild(row('background.type', bgTypeSel));
  secVisual.appendChild(row('background.colors', inputJson(bg.colors ?? ['#FFE0F7FF','#FFB3E5FC'], v => { bg.colors=v; sendLiveDebounced(); }, 4), '颜色数组'));
  secVisual.appendChild(row('background.border_radius', inputNumber(bg.border_radius ?? 8, v => { bg.border_radius=v|0; sendLiveDebounced(); })));
  secVisual.appendChild(row('background.rounded', inputBool(bg.rounded ?? true, v => { bg.rounded=!!v; sendLiveDebounced(); })));
  secVisual.appendChild(row('background.image_path', inputText(bg.image_path ?? '', v => { if(!v) delete bg.image_path; else bg.image_path=v; sendLiveDebounced(); })));
  formRoot.appendChild(secVisual);

  const secBehavior = section('Behavior');
  secBehavior.appendChild(row('default_duration', inputNumber(behavior.default_duration ?? -1, v => { behavior.default_duration=v|0; sendLiveDebounced(); }), '-1=永久'));
  secBehavior.appendChild(row('pause_timer_on_hover', inputBool(behavior.pause_timer_on_hover ?? true, v => { behavior.pause_timer_on_hover=!!v; sendLiveDebounced(); })));
  secBehavior.appendChild(row('closable_by_key', inputText(behavior.closable_by_key ?? '', v => { if(!v) delete behavior.closable_by_key; else behavior.closable_by_key=v; sendLiveDebounced(); }), 'key.keyboard.delete'));
  secBehavior.appendChild(row('allow_paging', inputBool(behavior.allow_paging ?? true, v => { behavior.allow_paging=!!v; sendLiveDebounced(); })));
  formRoot.appendChild(secBehavior);

  const secPage = section('Page 0');
  let page0 = pages.find(p => p.page_index === 0);
  if (!page0) { page0 = { page_index: 0 }; pages.push(page0); }
  const titleEl = ensureObj(page0,'title',{ text:{ text:'AuraTip Editor Preview' }, scale:0.85, line_spacing:0 });
  const contentEl = ensureObj(page0,'content',{ text:{ text:'Edit in browser, preview renders in-game.' }, scale:0.70, line_spacing:1 });
  secPage.appendChild(row('title.text (plain)', inputText((titleEl.text && titleEl.text.text) ? titleEl.text.text : '', v => { titleEl.text={ text:v }; sendLiveDebounced(); })));
  const contentArea = el('textarea','w-full h-20 text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  contentArea.value = (contentEl.text && contentEl.text.text) ? contentEl.text.text : '';
  contentArea.addEventListener('input', () => { contentEl.text={ text: contentArea.value }; sendLiveDebounced(); });
  secPage.appendChild(row('content.text (plain)', contentArea));
  secPage.appendChild(row('title.text (json)', inputJson(titleEl.text ?? {text:''}, v => { titleEl.text=v; sendLiveDebounced(); }, 6)));
  secPage.appendChild(row('content.text (json)', inputJson(contentEl.text ?? {text:''}, v => { contentEl.text=v; sendLiveDebounced(); }, 8)));
  secPage.appendChild(row('title.scale', inputNumber(titleEl.scale ?? 0.85, v => { titleEl.scale=v; sendLiveDebounced(); })));
  secPage.appendChild(row('content.scale', inputNumber(contentEl.scale ?? 0.70, v => { contentEl.scale=v; sendLiveDebounced(); })));
  formRoot.appendChild(secPage);
}

function renderSlotIcon(slot) {
  const isItem = (slot.icon && typeof slot.icon === 'object' && !Array.isArray(slot.icon));
  const wrap = el('div', 'space-y-2');

  const mainSel = el('select', 'w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  [{l:'texture', i:0},{l:'item', i:1}].forEach(v => {
    const o=el('option','',v.l); o.value=String(v.i); mainSel.appendChild(o);
  });
  mainSel.value = isItem ? '1' : '0';

  const body = el('div', 'space-y-2');

  // Track whether item mode use shorthand (string) or full (object)
  function isItemShorthand(icon) {
    return typeof icon === 'string';
  }

  function renderBody() {
    body.innerHTML = '';

    if (mainSel.value === '0') {
      // --- TEXTURE MODE ---
      if (typeof slot.icon !== 'string') slot.icon = 'minecraft:textures/item/paper.png';
      body.appendChild(row('icon', inputText(String(slot.icon || 'minecraft:textures/item/paper.png'), v => { slot.icon=v; sendLiveDebounced(); }), 'minecraft:textures/item/...'));
      return;
    }

    // --- ITEM MODE ---
    const shorthand = isItemShorthand(slot.icon);

    // sub-select: shorthand vs full
    const subSel = el('select', 'w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
    [{l:'shorthand (item id)', i:0},{l:'full (id + count + components)', i:1}].forEach(v => {
      const o=el('option','',v.l); o.value=String(v.i); subSel.appendChild(o);
    });
    subSel.value = shorthand ? '0' : '1';
    body.appendChild(row('format', subSel));

    function subRender() {
      body.innerHTML = '';
      body.appendChild(row('format', subSel));

      if (subSel.value === '0') {
        // SHORTHAND: just a string
        if (typeof slot.icon !== 'string') slot.icon = 'minecraft:chest';
        body.appendChild(row('id', inputText(String(slot.icon || 'minecraft:chest'), v => { slot.icon=v; sendLiveDebounced(); }), 'item id (e.g. minecraft:chest)'));
      } else {
        // FULL: object with fields
        if (typeof slot.icon !== 'object' || Array.isArray(slot.icon) || isItemShorthand(slot.icon)) {
          slot.icon = { type:'auratip:item', id:'minecraft:chest', count:1, components:{} };
        }
        const o = slot.icon;
        o.type = 'auratip:item';
        if (typeof (o.id) !== 'string') o.id = 'minecraft:chest';
        if (typeof (o.count) !== 'number' || isNaN(o.count)) o.count = 1;
        if (!o.components || typeof o.components !== 'object') o.components = {};
        // migrate from nested template format
        if (o.stack && typeof o.stack.id === 'string') {
          if (typeof o.stack.id === 'string') o.id = o.stack.id;
          if (typeof o.stack.count === 'number') o.count = o.stack.count;
          if (o.stack.components) o.components = o.stack.components;
          delete o.stack;
        }
        body.appendChild(row('id', inputText(String(o.id ?? 'minecraft:chest'), v => { o.id=v; sendLiveDebounced(); }), 'item id'));
        body.appendChild(row('count', inputNumber(Number(o.count) || 1, v => { o.count=v|0; sendLiveDebounced(); }), '1~99'));
        body.appendChild(row('components', inputJson(o.components && typeof o.components === 'object' ? o.components : {}, v => { o.components=v; sendLiveDebounced(); }, 4), 'DataComponentPatch JSON'));
      }
    }

    subSel.addEventListener('change', () => {
      if (subSel.value === '0') {
        // shorthand
        if (typeof slot.icon === 'object' && !Array.isArray(slot.icon)) {
          slot.icon = String(slot.icon.id || 'minecraft:chest');
        } else {
          slot.icon = 'minecraft:chest';
        }
      } else {
        // full object
        if (typeof slot.icon === 'string') {
          slot.icon = { type:'auratip:item', id: slot.icon || 'minecraft:chest', count:1, components:{} };
        }
      }
      subRender();
      sendLiveDebounced();
    });

    subRender();
  }

  mainSel.addEventListener('change', () => {
    if (mainSel.value === '1') {
      // switch to item: start with shorthand string
      slot.icon = typeof slot.icon === 'object' ? slot.icon : 'minecraft:chest';
    } else {
      // switch to texture
      slot.icon = 'minecraft:textures/item/paper.png';
    }
    renderBody();
    sendLiveDebounced();
  });

  renderBody();
  wrap.appendChild(mainSel);
  wrap.appendChild(body);
  return row('icon', wrap, undefined);
}

function renderRadialForm() {
  const menu = radialState; if (!menu) return;
  const settings = ensureObj(menu,'menu_settings',{});
  const slots = ensureArr(menu,'slots',[]);

  const secMenu = section('Menu');
  secMenu.appendChild(row('id', inputText(menu.id ?? 'auratip:editor_preview_menu', v => { menu.id=v; sendLiveDebounced(); })));
  secMenu.appendChild(row('inner_radius', inputNumber(settings.inner_radius ?? 55, v => { settings.inner_radius=v|0; sendLiveDebounced(); })));
  secMenu.appendChild(row('outer_radius', inputNumber(settings.outer_radius ?? 100, v => { settings.outer_radius=v|0; sendLiveDebounced(); })));
  secMenu.appendChild(row('animation_speed', inputNumber(settings.animation_speed ?? 1.0, v => { settings.animation_speed=v; sendLiveDebounced(); })));
  secMenu.appendChild(row('ring_color', inputText(settings.ring_color ?? '', v => { if(!v) delete settings.ring_color; else settings.ring_color=v; sendLiveDebounced(); }), 'ARGB'));
  secMenu.appendChild(row('ring_colors', inputJson(settings.ring_colors ?? null, v => { if(v===null) delete settings.ring_colors; else settings.ring_colors=v; sendLiveDebounced(); }, 4), '可选渐变数组'));
  formRoot.appendChild(secMenu);

  const secSlots = section('Slots');
  const addBtn = el('button','text-xs bg-purple-600/30 hover:bg-purple-600/50 px-4 py-2 rounded-xl transition', t('add_item'));
  addBtn.addEventListener('click', () => {
    slots.push({ name:'NewSlot', icon:'minecraft:textures/item/paper.png', action:{ type:'auratip:run_command', command:'/say hello' }, text:{ text:'NewSlot' }, highlight_color:'#77FFFFFF' });
    renderForm(); sendLiveDebounced();
  });
  secSlots.appendChild(addBtn);

  slots.forEach((slot, idx) => {
    const card = el('div','mt-3 p-3 rounded-2xl bg-black/20 border border-white/10 space-y-3');
    const head = el('div','flex items-center justify-between');
    head.appendChild(el('div','text-xs font-semibold text-white/70', `#${idx} ${slot.name || ''}`));
    const del = el('button','text-xs bg-rose-600/30 hover:bg-rose-600/50 px-3 py-1.5 rounded-lg transition', t('delete'));
    del.addEventListener('click', () => { slots.splice(idx,1); renderForm(); sendLiveDebounced(); });
    head.appendChild(del);
    card.appendChild(head);

    card.appendChild(row('name', inputText(slot.name ?? '', v => { slot.name=v; renderForm(); sendLiveDebounced(); })));
    card.appendChild(renderSlotIcon(slot));
    card.appendChild(row('highlight_color', inputText(slot.highlight_color ?? '', v => { if(!v) delete slot.highlight_color; else slot.highlight_color=v; sendLiveDebounced(); }), 'ARGB'));

    slot.text = (slot.text && typeof slot.text === 'object') ? slot.text : { text:'' };
    card.appendChild(row('text (plain)', inputText(slot.text.text ?? '', v => { slot.text={ text:v }; sendLiveDebounced(); })));
    card.appendChild(row('text (json)', inputJson(slot.text, v => { slot.text=v; sendLiveDebounced(); }, 6)));

    slot.action = (slot.action && typeof slot.action === 'object') ? slot.action : { type:'auratip:run_command' };
    const actSel = el('select','w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
    (initPayload.actionTypes || []).forEach(t => { const o=el('option','',t); o.value=t; actSel.appendChild(o); });
    actSel.value = slot.action.type || 'auratip:run_command';
    actSel.addEventListener('change', () => {
      const nextType = actSel.value;
      const defs = actionDefs(nextType);
      const next = { type: nextType };
      if (Array.isArray(defs)) defs.forEach(d => { if(d && d.name) next[d.name] = (d.default !== undefined) ? d.default : ''; });
      slot.action = next;
      renderForm(); sendLiveDebounced();
    });
    card.appendChild(row('action.type', actSel));

    const defs = actionDefs(slot.action.type || '');
    if (Array.isArray(defs)) {
      defs.forEach(d => {
        const name=d.name, kind=d.kind||'string';
        const cur = (slot.action[name] !== undefined) ? slot.action[name] : (d.default !== undefined ? d.default : '');
        const control = (kind === 'number') ? inputNumber(cur, v => { slot.action[name]=v; sendLiveDebounced(); }) : inputText(cur, v => { slot.action[name]=v; sendLiveDebounced(); });
        card.appendChild(row(`action.${name}`, control));
      });
    } else {
      const raw = { ...slot.action }; delete raw.type;
      card.appendChild(row('action.params (raw)', inputJson(raw, v => { slot.action = { type: slot.action.type, ...(v||{}) }; sendLiveDebounced(); }, 8), '无 schema：JSON map'));
    }
    secSlots.appendChild(card);
  });

  formRoot.appendChild(secSlots);
}

function buildPositionControl(parent, key) {
  const wrap = el('div','space-y-2');
  const sel = el('select','w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  const presets = ['TOP_LEFT','TOP_CENTER','TOP_RIGHT','LEFT_CENTER','CENTER','RIGHT_CENTER','BOTTOM_LEFT','BOTTOM_CENTER','BOTTOM_RIGHT'];
  presets.forEach(p => { const o=el('option','',p); o.value=p; sel.appendChild(o); });
  const oAbs = el('option','', 'ABSOLUTE (x,y)'); oAbs.value='__ABS__'; sel.appendChild(oAbs);

  const v = parent[key];
  const isAbs = Array.isArray(v);
  sel.value = isAbs ? '__ABS__' : (v || 'CENTER');

  const rowXY = el('div','grid grid-cols-12 gap-2');
  const inX = el('input','col-span-6 w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  const inY = el('input','col-span-6 w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  inX.type='number'; inY.type='number';
  const xy = isAbs ? v : [0,0];
  inX.value = xy[0] ?? 0; inY.value = xy[1] ?? 0;
  rowXY.appendChild(inX); rowXY.appendChild(inY);
  function apply(){ rowXY.style.display = (sel.value === '__ABS__') ? 'grid' : 'none'; }
  apply();

  sel.addEventListener('change', () => { parent[key] = (sel.value === '__ABS__') ? [Number(inX.value||0), Number(inY.value||0)] : sel.value; apply(); sendLiveDebounced(); });
  inX.addEventListener('input', () => { if(sel.value!=='__ABS__') return; parent[key]=[Number(inX.value||0), Number(inY.value||0)]; sendLiveDebounced(); });
  inY.addEventListener('input', () => { if(sel.value!=='__ABS__') return; parent[key]=[Number(inX.value||0), Number(inY.value||0)]; sendLiveDebounced(); });
  wrap.appendChild(sel); wrap.appendChild(rowXY);
  return wrap;
}

function renderPositionControl(parent, key) {
  return row(key, buildPositionControl(parent, key), t('pos_help'));
}

function rebuildAnimParamsInto(visual, hover) {
  const styleKey = hover ? 'hover_animation_style' : 'animation_style';
  const paramsKey = hover ? 'hover_animation_params' : 'animation_params';
  const styleId = visual[styleKey] || '';
  const defs = hover ? hoverParamDefs(styleId) : transitionParamDefs(styleId);
  if (!defs || !defs.length) return;
  const out = {};
  defs.forEach(d => { if(d && d.name) out[d.name] = (d.default !== undefined) ? d.default : (d.kind === 'boolean' ? false : ''); });
  visual[paramsKey] = out;
}

function renderAnimParamsBox(visual, hover, canRemove=false, onRemove=null) {
  const paramsKey = hover ? 'hover_animation_params' : 'animation_params';
  const styleKey = hover ? 'hover_animation_style' : 'animation_style';
  const styleId = visual[styleKey] || '';
  const defs = hover ? hoverParamDefs(styleId) : transitionParamDefs(styleId);
  if (!visual[paramsKey] || typeof visual[paramsKey] !== 'object') visual[paramsKey] = {};
  const params = visual[paramsKey];

  const box = el('div','p-3 rounded-2xl bg-black/20 border border-white/10 space-y-3');
  const head = el('div', 'flex items-center justify-between');
  head.appendChild(el('div','text-xs font-semibold text-white/70', paramsKey));
  if (canRemove && typeof onRemove === 'function') {
    const rm = el('button', 'text-[11px] px-2 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-white/50 transition', t('remove'));
    rm.addEventListener('click', onRemove);
    head.appendChild(rm);
  }
  box.appendChild(head);
  if (defs && defs.length) {
    defs.forEach(d => {
      const name=d.name, kind=d.kind||'string';
      const cur = (params[name] !== undefined) ? params[name] : (d.default !== undefined ? d.default : '');
      let control;
      if (kind === 'number') control = inputNumber(cur, v => { params[name]=v; sendLiveDebounced(); });
      else if (kind === 'boolean') control = inputBool(cur, v => { params[name]=!!v; sendLiveDebounced(); });
      else control = inputText(cur, v => { params[name]=v; sendLiveDebounced(); });
      box.appendChild(row(name, control));
    });
    return box;
  }
  const openRaw = el('button', 'w-full text-xs bg-white/10 hover:bg-white/20 px-4 py-2 rounded-xl transition', t('open_json_inspector'));
  openRaw.addEventListener('click', () => {
    animEditor.classList.add('translate-x-[120%]');
    jsonInspector.classList.remove('translate-x-[120%]');
    refreshJsonArea(true);
  });
  box.appendChild(openRaw);
  return box;
}

function renderDataComponentSelector(parent, key, compTypes) {
  if (!parent[key] || typeof parent[key] !== 'object') parent[key] = {};
  const data = parent[key];
  const box = el('div', 'space-y-2');

  const addBtn = el('button', 'w-full text-xs bg-white/10 hover:bg-white/20 px-4 py-2 rounded-xl transition', '+ add component');
  addBtn.addEventListener('click', () => {
    const unused = compTypes.filter(t => !(t in data));
    if (!unused.length) return;
    data[unused[0]] = '';
    render();
    sendLiveDebounced();
  });
  box.appendChild(addBtn);

  function render() {
    while (box.children.length > 1) box.removeChild(box.lastChild);
    const keys = Object.keys(data).filter(k => compTypes.includes(k));
    if (keys.length === 0) {
      box.appendChild(el('div', 'text-[11px] text-white/25', 'No components added. Click "+ add component" above.'));
      return;
    }
    keys.forEach(k => {
      const row = el('div', 'flex items-start gap-1.5');
      // key select — fixed width, truncated text
      const sel = el('select', 'w-28 shrink-0 text-xs px-1.5 py-1.5 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40 overflow-hidden');
      compTypes.forEach(t => {
        const o = el('option', '', t);
        o.value = t;
        sel.appendChild(o);
      });
      sel.value = k;
      sel.addEventListener('change', () => {
        const oldVal = data[k];
        delete data[k];
        data[sel.value] = oldVal;
        render();
        sendLiveDebounced();
      });
      row.appendChild(sel);
      // value input — JSON text, validated on blur
      const valInput = el('textarea', 'flex-1 text-xs px-2 py-1.5 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
      valInput.style.minHeight = '24px';
      valInput.style.resize = 'vertical';
      valInput.style.fontFamily = 'monospace';
      valInput.placeholder = '{"potion":"minecraft:fire_resistance"}';
      valInput.value = (typeof data[k] === 'string') ? data[k] : JSON.stringify(data[k], null, 2);
      function parseValue() {
        const v = valInput.value.trim();
        if (!v) { data[k] = {}; sendLiveDebounced(); return; }
        try { data[k] = JSON.parse(v); sendLiveDebounced(); } catch(e) {}
      }
      valInput.addEventListener('blur', parseValue);
      valInput.addEventListener('keydown', (e) => { if (e.key === 'Enter' && e.ctrlKey) { parseValue(); valInput.focus(); } });
      row.appendChild(valInput);
      // remove
      const rm = el('button', 'shrink-0 text-xs bg-rose-600/30 hover:bg-rose-600/50 px-2 py-1.5 rounded-lg transition', '×');
      rm.addEventListener('click', () => {
        delete data[k];
        render();
        sendLiveDebounced();
      });
      row.appendChild(rm);
      box.appendChild(row);
    });
  }
  render();
  return box;
}
