// -----------------------------
// Utility functions
// -----------------------------

function safeJsonParse(str){ try{ return JSON.parse(str); }catch{ return null; } }

function setConn(state, text) {
  connPill.textContent = text;
  if (state === 'ok') connPill.className = 'glass px-3 py-1.5 rounded-lg text-xs text-emerald-200 border-emerald-500/30';
  else if (state === 'bad') connPill.className = 'glass px-3 py-1.5 rounded-lg text-xs text-rose-200 border-rose-500/30';
  else connPill.className = 'glass px-3 py-1.5 rounded-lg text-xs text-slate-300';
}

function deepClone(v) {
  return JSON.parse(JSON.stringify(v));
}

function pathNoIndex(path) {
  return (path || []).filter(p => typeof p === 'string');
}

function endsWithPath(path, tail) {
  const p = pathNoIndex(path);
  if (p.length < tail.length) return false;
  for (let i = 0; i < tail.length; i++) {
    if (p[p.length - tail.length + i] !== tail[i]) return false;
  }
  return true;
}

function toHex2(n) {
  const v = Math.max(0, Math.min(255, Number(n) | 0));
  return v.toString(16).toUpperCase().padStart(2, '0');
}

function parseHexColor(str) {
  if (str === null || str === undefined) return null;
  const raw = String(str).trim();
  if (!raw) return null;
  const v = raw.startsWith('#') ? raw.slice(1) : raw;
  if (!/^[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$/.test(v)) return null;
  if (v.length === 6) {
    const r = parseInt(v.slice(0, 2), 16);
    const g = parseInt(v.slice(2, 4), 16);
    const b = parseInt(v.slice(4, 6), 16);
    return { r, g, b, a: 255, hasAlpha: false };
  }
  // AARRGGBB
  const a = parseInt(v.slice(0, 2), 16);
  const r = parseInt(v.slice(2, 4), 16);
  const g = parseInt(v.slice(4, 6), 16);
  const b = parseInt(v.slice(6, 8), 16);
  return { r, g, b, a, hasAlpha: true };
}

function rgbaToHex(rgba, allowAlpha) {
  if (!rgba) return '';
  const rr = toHex2(rgba.r);
  const gg = toHex2(rgba.g);
  const bb = toHex2(rgba.b);
  const aa = toHex2(rgba.a);
  if (!allowAlpha) {
    return `#${rr}${gg}${bb}`;
  }
  if ((rgba.a | 0) === 255) {
    return `#${rr}${gg}${bb}`;
  }
  return `#${aa}${rr}${gg}${bb}`;
}

function isSchemaColorPath(path) {
  const p = pathNoIndex(path);
  const s = p.join('.');
  if (s.endsWith('theme_color')) return { allowAlpha: true };
  if (s.endsWith('ring_color')) return { allowAlpha: true };
  if (s.endsWith('ring_colors')) return { allowAlpha: true, arrayItem: true };
  if (s.endsWith('background.colors')) return { allowAlpha: true, arrayItem: true };
  if (s.endsWith('highlight_color')) return { allowAlpha: false };
  if (s.endsWith('divider.color')) return { allowAlpha: true };
  if (s.endsWith('color') && (s.includes('divider') || s.includes('background'))) return { allowAlpha: true };
  if (s.endsWith('badge.background_color')) return { allowAlpha: true };
  if (s.endsWith('shadow.color')) return { allowAlpha: true };
  return null;
}

function el(tag, cls, text) {
  const e = document.createElement(tag);
  if (cls) e.className = cls;
  if (text !== undefined) e.textContent = text;
  return e;
}

function row(label, control, help) {
  const r = el('div', 'space-y-1');
  r.appendChild(el('div', 'text-[11px] text-white/50', label));
  r.appendChild(control);
  if (help) r.appendChild(el('div', 'text-[11px] text-white/25', help));
  return r;
}

function inputText(value, onChange) {
  const input = el('input', 'w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  input.type = 'text';
  input.value = value ?? '';
  input.addEventListener('input', () => onChange(input.value));
  return input;
}

function inputNumber(value, onChange) {
  const input = el('input', 'w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  input.type = 'number';
  input.step = 'any';
  input.value = (value ?? 0);
  input.addEventListener('input', () => onChange(Number(input.value)));
  return input;
}

function inputBool(value, onChange) {
  const wrap = el('label', 'flex items-center gap-2 text-xs text-white/80 select-none');
  const input = el('input', 'accent-blue-500');
  input.type = 'checkbox';
  input.checked = !!value;
  input.addEventListener('change', () => onChange(input.checked));
  wrap.appendChild(input);
  wrap.appendChild(el('span', 'text-white/60', input.checked ? 'true' : 'false'));
  input.addEventListener('change', () => wrap.lastChild.textContent = input.checked ? 'true' : 'false');
  return wrap;
}

function inputJson(value, onChange, rows=6) {
  const area = el('textarea', 'code w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  area.style.height = (rows * 18) + 'px';
  area.value = JSON.stringify(value ?? {}, null, 2);
  area.addEventListener('input', () => {
    const v = safeJsonParse(area.value);
    if (v !== null) onChange(v);
  });
  return area;
}

function colorPalette(value, onChange, opts) {
  const allowAlpha = !!(opts && opts.allowAlpha);

  // For RGB-only fields (like highlight_color), strip alpha if user pasted AARRGGBB.
  let rgba = parseHexColor(value);
  if (!rgba) rgba = { r: 255, g: 255, b: 255, a: 255, hasAlpha: false };
  if (!allowAlpha && rgba.hasAlpha) {
    rgba = { r: rgba.r, g: rgba.g, b: rgba.b, a: 255, hasAlpha: false };
  }

  const wrap = el('div', 'space-y-2');
  const top = el('div', 'flex items-center gap-2');

  const picker = el('input', 'h-9 w-12 rounded-lg bg-black/20 border border-white/10');
  picker.type = 'color';
  picker.value = `#${toHex2(rgba.r)}${toHex2(rgba.g)}${toHex2(rgba.b)}`;

  const txt = el('input', 'code w-full text-xs px-3 py-2 rounded-xl bg-black/20 border border-white/10 outline-none focus:border-blue-500/40');
  txt.type = 'text';
  txt.value = rgbaToHex(rgba, allowAlpha);
  txt.placeholder = allowAlpha ? '#RRGGBB / #AARRGGBB' : '#RRGGBB';

  top.appendChild(picker);
  top.appendChild(txt);
  wrap.appendChild(top);

  let alpha = rgba.a | 0;
  let alphaRow = null;
  let alphaInput = null;
  let alphaValue = null;

  function emit() {
    const hex = rgbaToHex({ r: rgba.r, g: rgba.g, b: rgba.b, a: alpha }, allowAlpha);
    txt.value = hex;
    picker.value = `#${toHex2(rgba.r)}${toHex2(rgba.g)}${toHex2(rgba.b)}`;
    if (alphaValue) alphaValue.textContent = allowAlpha ? String(alpha) : '';
    onChange(hex);
  }

  if (allowAlpha) {
    alphaRow = el('div', 'flex items-center gap-2');
    alphaInput = el('input', 'w-full');
    alphaInput.type = 'range';
    alphaInput.min = '0';
    alphaInput.max = '255';
    alphaInput.value = String(alpha);
    alphaValue = el('div', 'code text-[11px] text-white/40 w-12 text-right', String(alpha));
    alphaRow.appendChild(el('div', 'text-[11px] text-white/35 w-10', 'alpha'));
    alphaRow.appendChild(alphaInput);
    alphaRow.appendChild(alphaValue);
    wrap.appendChild(alphaRow);

    alphaInput.addEventListener('input', () => {
      alpha = Number(alphaInput.value || 255) | 0;
      emit();
    });
  }

  picker.addEventListener('input', () => {
    const pv = String(picker.value || '').trim();
    const parsed = parseHexColor(pv);
    if (parsed) {
      rgba.r = parsed.r;
      rgba.g = parsed.g;
      rgba.b = parsed.b;
      emit();
    }
  });

  txt.addEventListener('input', () => {
    const parsed = parseHexColor(txt.value);
    if (!parsed) return;
    rgba.r = parsed.r;
    rgba.g = parsed.g;
    rgba.b = parsed.b;
    alpha = allowAlpha ? (parsed.hasAlpha ? parsed.a : 255) : 255;
    if (alphaInput) alphaInput.value = String(alpha);
    emit();
  });

  return wrap;
}

function section(title) {
  const s = el('div', 'glass rounded-2xl p-4 space-y-3');
  s.appendChild(el('div', 'text-sm font-semibold', title));
  return s;
}

function ensureObj(parent, key, fallback={}) {
  if (!parent[key] || typeof parent[key] !== 'object') parent[key] = fallback;
  return parent[key];
}
function ensureArr(parent, key, fallback=[]) {
  if (!Array.isArray(parent[key])) parent[key] = fallback;
  return parent[key];
}

function unsetBtn(onClick) {
  const b = el('button', 'text-[11px] px-2 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-white/50 transition', t('unset'));
  b.addEventListener('click', onClick);
  return b;
}

function stringWithUnset(obj, key, placeholder) {
  const wrap = el('div', 'flex items-center gap-2');
  const input = inputText(obj[key] ?? '', v => { obj[key] = v; sendLiveDebounced(); });
  if (placeholder) input.placeholder = placeholder;
  wrap.appendChild(input);
  wrap.appendChild(unsetBtn(() => { delete obj[key]; input.value = ''; sendLiveDebounced(); }));
  return wrap;
}

function boolWithUnset(obj, key) {
  const wrap = el('div', 'flex items-center justify-between gap-2');
  const v = obj[key];
  const chk = inputBool(!!v, nv => { obj[key] = !!nv; sendLiveDebounced(); });
  wrap.appendChild(chk);
  wrap.appendChild(unsetBtn(() => { delete obj[key]; sendLiveDebounced(); }));
  return wrap;
}

function ensureComponentValue(v) {
  if (v === null || v === undefined) return { text: '' };
  if (typeof v === 'string') return { text: v };
  if (typeof v !== 'object' || Array.isArray(v)) return { text: String(v) };
  return v;
}

function jsLiteral(v) {
  if (v === null || v === undefined) return 'null';
  if (typeof v === 'number' || typeof v === 'boolean') return String(v);
  if (typeof v === 'string') return JSON.stringify(v);
  return JSON.stringify(v);
}

function isPlainObject(v) {
  return v !== null && typeof v === 'object' && !Array.isArray(v);
}

function sanitizeComponentJson(v) {
  if (Array.isArray(v)) {
    const out = v.map(sanitizeComponentJson).filter(x => x !== undefined);
    return out;
  }
  if (!isPlainObject(v)) {
    return v;
  }

  const out = {};
  for (const [k, rawVal] of Object.entries(v)) {
    if (rawVal === undefined || rawVal === null) continue;

    // AuraTip editor: filter out vanilla-only fields we don't support in this workflow.
    if (k === 'insertion' || k === 'clickEvent' || k === 'hoverEvent' || k === 'font') {
      continue;
    }

    // Drop explicit default false style flags to keep exports minimal.
    if (k === 'bold' || k === 'italic' || k === 'underlined' || k === 'strikethrough' || k === 'obfuscated') {
      if (rawVal === true) out[k] = true;
      continue;
    }

    if (k === 'extra' || k === 'with') {
      if (!Array.isArray(rawVal) || rawVal.length === 0) continue;
      out[k] = rawVal.map(sanitizeComponentJson);
      continue;
    }

    if (isPlainObject(rawVal)) {
      out[k] = sanitizeComponentJson(rawVal);
      continue;
    }

    out[k] = rawVal;
  }
  return out;
}

function downloadText(filename, content, mime) {
  const blob = new Blob([content], { type: mime || 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 500);
}

function getParamMeta() { return (initPayload && initPayload.paramMeta) ? initPayload.paramMeta : {}; }
function transitionParamDefs(id){ const m=getParamMeta(); return (m.animation && m.animation.transition && m.animation.transition[id]) ? m.animation.transition[id] : []; }
function hoverParamDefs(id){ const m=getParamMeta(); return (m.animation && m.animation.hover && m.animation.hover[id]) ? m.animation.hover[id] : []; }
function actionDefs(type){ const m=getParamMeta(); return (m.actions && m.actions[type]) ? m.actions[type] : null; }
