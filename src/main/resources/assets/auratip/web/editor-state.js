
const mainMenu = document.getElementById('mainMenu');
const leftPanel = document.getElementById('leftPanel');
const formRoot = document.getElementById('formRoot');
const editorTag = document.getElementById('editorTag');
const panelTitle = document.getElementById('panelTitle');
const currentId = document.getElementById('currentId');
const connPill = document.getElementById('connPill');
const previewLine1 = document.getElementById('previewLine1');
const previewLine2 = document.getElementById('previewLine2');

const animEditor = document.getElementById('animEditor');
const btnToggleAnim = document.getElementById('btnToggleAnim');
const btnLang = document.getElementById('btnLang');
const btnJson = document.getElementById('btnJson');
const btnRunAnim = document.getElementById('btnRunAnim');
const btnCopyAnimJava = document.getElementById('btnCopyAnimJava');
const animKind = document.getElementById('animKind');
const animJs = document.getElementById('animJs');
const animStatus = document.getElementById('animStatus');
const animId = document.getElementById('animId');
const animDefaults = document.getElementById('animDefaults');
const animTestParams = document.getElementById('animTestParams');
const btnUseDefaultsAsTest = document.getElementById('btnUseDefaultsAsTest');

const jsonInspector = document.getElementById('jsonInspector');
const jsonArea = document.getElementById('jsonArea');
const jsonStatus = document.getElementById('jsonStatus');
const btnJsonApply = document.getElementById('btnJsonApply');
const btnJsonSync = document.getElementById('btnJsonSync');
const btnJsonRevert = document.getElementById('btnJsonRevert');
const btnJsonCopy = document.getElementById('btnJsonCopy');

const btnCopyJson = document.getElementById('btnCopyJson');
const btnExport = document.getElementById('btnExport');
const exportMenu = document.getElementById('exportMenu');
const exportJson = document.getElementById('exportJson');
const exportJs = document.getElementById('exportJs');
const exportJava = document.getElementById('exportJava');
const btnClose = document.getElementById('btnClose');
const btnSendNow = document.getElementById('btnSendNow');

let ws = null;
let initPayload = null;
let mode = null; // 'tip' | 'radial'
let tipState = null;
let radialState = null;
let debounceTimer = null;

// JSON Inspector
let jsonDirty = false;
let jsonLastRendered = '';

// Animation
let animJavaLastGenerated = '';
let animJavaDirty = false;
