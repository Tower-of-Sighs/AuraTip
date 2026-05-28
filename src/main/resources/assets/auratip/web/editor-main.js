lucide.createIcons();

// -----------------------------
// Language / startup
// -----------------------------

(async () => {
  await Promise.all([loadLangFile('en'), loadLangFile('zh')]);
  applyI18n();
  renderForm();
})();

btnLang.addEventListener('click', () => {
  LANG = (LANG === 'en') ? 'zh' : 'en';
  localStorage.setItem('auratip_lang', LANG);
  applyI18n();
  renderForm();
});

// -----------------------------
// Editor open / close
// -----------------------------

function openEditor(nextMode) {
  mode = nextMode;
  mainMenu.classList.add('hidden');
  leftPanel.classList.remove('-translate-x-[120%]');
  editorTag.classList.remove('hidden');

  if (mode === 'radial') {
    editorTag.innerText = 'RADIAL_MENU_MODE';
    panelTitle.innerText = t('radial_card_title');
  } else {
    editorTag.innerText = 'TIP_BUILDER_MODE';
    panelTitle.innerText = t('tip_card_title');
  }

  send({ type: 'set_mode', mode });
  renderForm();
  sendLiveDebounced();
}

function closeEditors() {
  const prev = mode;
  mode = null;
  mainMenu.classList.remove('hidden');
  leftPanel.classList.add('-translate-x-[120%]');
  animEditor.classList.add('translate-x-[120%]');
  jsonInspector.classList.add('translate-x-[120%]');
  editorTag.classList.add('hidden');

  // Close the in-game preview for the editor we just exited.
  if (prev) {
    send({ type: 'close_preview', mode: prev });
  }
}

window.openEditor = openEditor;
window.closeEditors = closeEditors;

// -----------------------------
// Panel toggle buttons
// -----------------------------

btnClose.addEventListener('click', closeEditors);
btnToggleAnim.addEventListener('click', () => {
  jsonInspector.classList.add('translate-x-[120%]');
  animEditor.classList.toggle('translate-x-[120%]');
});
btnJson.addEventListener('click', () => {
  animEditor.classList.add('translate-x-[120%]');
  jsonInspector.classList.toggle('translate-x-[120%]');
  refreshJsonArea(true);
});
btnSendNow.addEventListener('click', () => sendLiveNow());
btnRunAnim.addEventListener('click', () => {
  animStatus.textContent = 'APPLYING...';
  const params = safeJsonParse(animTestParams.value || '{}');
  send({
    type: 'animation_apply',
    kind: animKind.value,
    id: animId.value || '',
    params: (params && typeof params === 'object') ? params : {}
  });
});

btnUseDefaultsAsTest.addEventListener('click', () => {
  const d = safeJsonParse(animDefaults.value || '{}');
  animTestParams.value = JSON.stringify((d && typeof d === 'object') ? d : {}, null, 2);
});

// -----------------------------
// Animation editor listeners
// -----------------------------

animJs.addEventListener('input', () => {
  animJavaDirty = (animJs.value !== animJavaLastGenerated);
});
animKind.addEventListener('change', () => refreshAnimJavaSnippet(false));
animId.addEventListener('input', () => refreshAnimJavaSnippet(false));
animDefaults.addEventListener('input', () => refreshAnimJavaSnippet(false));

btnCopyAnimJava.addEventListener('click', async () => {
  refreshAnimJavaSnippet(false);
  const out = animJs.value || buildAnimJavaSnippet();
  try { await navigator.clipboard.writeText(out); } catch {}
});

btnCopyJson.addEventListener('click', async () => {
  const obj = (mode === 'radial') ? radialState : tipState;
  const text = JSON.stringify(obj || {}, null, 2);
  try { await navigator.clipboard.writeText(text); } catch {}
});

// -----------------------------
// Export
// -----------------------------

btnExport.addEventListener('click', (e) => {
  e.stopPropagation();
  toggleExportMenu();
});

document.addEventListener('click', () => {
  if (!exportMenu.classList.contains('hidden')) {
    exportMenu.classList.add('hidden');
  }
});

exportJson.addEventListener('click', () => { exportMenu.classList.add('hidden'); exportCurrentJson(); });
exportJs.addEventListener('click', () => { exportMenu.classList.add('hidden'); exportKubeJs(); });
exportJava.addEventListener('click', () => { exportMenu.classList.add('hidden'); exportJavaSnippet(); });

// -----------------------------
// JSON Inspector listeners
// -----------------------------

jsonArea.addEventListener('input', () => {
  jsonDirty = true;
  parseJsonArea();
});

btnJsonApply.addEventListener('click', () => {
  const v = parseJsonArea();
  if (v === null) return;
  if (mode === 'radial') radialState = v;
  else tipState = v;
  jsonDirty = false;
  renderForm();
  sendLiveDebounced();
  refreshJsonArea(true);
});

btnJsonSync.addEventListener('click', () => sendLiveNow());

btnJsonRevert.addEventListener('click', () => {
  jsonDirty = false;
  refreshJsonArea(true);
});

btnJsonCopy.addEventListener('click', async () => {
  try { await navigator.clipboard.writeText(jsonArea.value || ''); } catch {}
});

// -----------------------------
// WebSocket init
// -----------------------------

connectWs();
