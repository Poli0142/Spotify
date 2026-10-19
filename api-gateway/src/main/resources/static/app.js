const $ = id => document.getElementById(id);
const content = $('content');
const roles = {USER: 'Слушатель', ARTIST: 'Исполнитель', ADMIN: 'Администратор'};

let me = null;
let myArtistId = null;
let likedIds = new Set();
let myPlaylists = [];
let view = {page: 'home'};
let lists = {};
let queue = [];
let index = -1;
let position = 0;
let timer = null;
let playing = false;
let duration = 0;
let widget = null;
let useWidget = false;
let shuffle = false;
let repeat = 'off';

async function api(url, options = {}) {
    const res = await fetch(url, {headers: {'Content-Type': 'application/json'}, ...options});
    if (res.status === 204) return null;
    const data = await res.json().catch(() => null);
    if (!res.ok) {
        const error = new Error(data && data.message ? data.message : 'Ошибка ' + res.status);
        error.status = res.status;
        throw error;
    }
    return data;
}

const post = (url, body) => api(url, {method: 'POST', body: JSON.stringify(body)});
const del = url => api(url, {method: 'DELETE'});

function esc(s) {
    return String(s ?? '').replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
}

function time(sec) {
    sec = Math.floor(sec || 0);
    return Math.floor(sec / 60) + ':' + String(sec % 60).padStart(2, '0');
}

function date(s) {
    return new Date(s).toLocaleString('ru-RU', {day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'});
}

function gradient(seed) {
    let h = 0;
    for (const c of String(seed)) h = (h * 31 + c.charCodeAt(0)) % 360;
    return `linear-gradient(135deg, hsl(${h}, 55%, 45%), hsl(${(h + 50) % 360}, 55%, 22%))`;
}

function cover(seed, text, cls = '') {
    return `<div class="cover ${cls}" style="background:${gradient(seed)}">${esc(String(text || '?')[0].toUpperCase())}</div>`;
}

function toast(text, error) {
    const t = $('toast');
    t.textContent = text;
    t.className = 'show' + (error ? ' error' : '');
    clearTimeout(t.timer);
    t.timer = setTimeout(() => t.className = '', 3500);
}

function header(seed, title, type, sub, cls = '') {
    return `<div class="header">${cover(seed, title, cls)}<div><div class="type">${type}</div><h1>${esc(title)}</h1><div class="muted">${sub}</div></div></div>`;
}

const artistCard = a => `<div class="card" data-artist="${a.id}">${cover(a.name, a.name, 'round')}<div class="title">${esc(a.name)}</div><div class="muted">${esc(a.genre)}</div></div>`;

const albumCard = a => `<div class="card" data-album="${a.id}">${cover(a.title, a.title)}<div class="title">${esc(a.title)}</div><div class="muted">${a.releaseYear} · ${esc(a.artist.name)}</div></div>`;

const playAll = '<div class="actions"><button class="big-play" data-playall>&#9654;</button></div>';

const heart = id => likedIds.has(id) ? '&#9829;' : '&#9825;';

const likeButton = id => `<button class="row-btn heart${likedIds.has(id) ? ' liked' : ''}" data-like="${id}" title="Любимые">${heart(id)}</button>`;

const canDelete = t => me.role === 'ADMIN' || (me.role === 'ARTIST' && myArtistId !== null && t.artistId === myArtistId);

function trackTable(tracks, opts = {}) {
    if (!tracks.length) return '<div class="empty">Здесь пока пусто</div>';
    const id = 'l' + Object.keys(lists).length;
    lists[id] = tracks;
    const now = queue[index];
    const rows = tracks.map((t, i) => {
        const off = t.available === false || !t.title;
        const artist = t.artistId ? `<a data-artist="${t.artistId}">${esc(t.artistName)}</a>` : esc(t.artistName || '');
        const buttons = off ? '' : likeButton(t.id)
            + `<button class="row-btn" data-add="${t.id}" title="Добавить в плейлист">+</button>`
            + (opts.playlistId ? `<button class="row-btn" data-remove="${t.id}" title="Убрать из плейлиста">&times;</button>` : '')
            + (canDelete(t) ? `<button class="row-btn" data-delete-track="${t.id}" title="Удалить трек">&#128465;</button>` : '');
        return `<tr class="row${off ? ' off' : ''}${now && now.id === t.id ? ' playing' : ''}" data-list="${id}" data-i="${i}">
            <td><span class="num">${i + 1}</span><span class="num-play">&#9654;</span></td>
            <td><div class="track-cell">${cover(t.albumTitle || t.title, t.albumTitle || t.title, 'mini')}<div><div class="name">${off ? 'Трек недоступен' : esc(t.title)}</div><div class="muted">${artist}</div></div></div></td>
            <td class="muted hide-sm">${esc(t.albumTitle || '')}</td>
            ${opts.extra ? `<td class="muted hide-sm">${esc(opts.extra.value(t))}</td>` : ''}
            <td>${buttons}<span class="muted">${off ? '-' : time(t.durationSec)}</span></td>
        </tr>`;
    }).join('');
    return `<table class="tracks"><tr><th>#</th><th>Название</th><th class="hide-sm">Альбом</th>${opts.extra ? `<th class="hide-sm">${opts.extra.title}</th>` : ''}<th></th></tr>${rows}</table>`;
}

async function section(id, title, promise, render) {
    const el = $(id);
    const head = title ? `<h2>${title}</h2>` : '';
    try {
        const data = await promise;
        if (document.body.contains(el)) el.innerHTML = head + render(data);
    } catch (e) {
        if (document.body.contains(el)) el.innerHTML = head + `<div class="empty">${esc(e.message)}</div>`;
    }
}

const plays = {title: 'Прослушивания', value: t => t.plays};
const topTracks = top => top.map(t => ({...t.track, plays: t.plays}));

const pages = {
    home() {
        const hour = new Date().getHours();
        const hello = hour < 6 ? 'Доброй ночи' : hour < 12 ? 'Доброе утро' : hour < 18 ? 'Добрый день' : 'Добрый вечер';
        content.innerHTML = `<h1>${hello}, ${esc(me.displayName)}</h1><div id="s1"></div><div id="s2"></div><div id="s3"></div><div id="s4"></div>`;
        section('s1', 'Исполнители', api('/api/artists'), list => `<div class="grid">${list.map(artistCard).join('')}</div>`);
        section('s2', 'Популярное сейчас', api('/api/stream/top?limit=5'), top => trackTable(topTracks(top), {extra: plays}));
        section('s3', 'Синглы и альбомы', api('/api/albums'), list => `<div class="grid">${list.map(albumCard).join('')}</div>`);
        section('s4', 'Все треки', api('/api/tracks'), tracks => trackTable(tracks));
    },

    search() {
        content.innerHTML = '<h1>Поиск</h1><input class="search" id="q" placeholder="Что хочешь послушать?" autocomplete="off"><div id="r1"></div><div id="r2"></div>';
        const q = $('q');
        let wait;
        const run = () => {
            const text = q.value.trim().toLowerCase();
            lists = {};
            $('r1').innerHTML = '';
            if (text) {
                section('r1', 'Исполнители', api('/api/artists').then(a => a.filter(x => x.name.toLowerCase().includes(text))),
                    list => list.length ? `<div class="grid">${list.map(artistCard).join('')}</div>` : '<div class="empty">Не найдено</div>');
            }
            section('r2', text ? 'Треки' : 'Все треки', api('/api/tracks?search=' + encodeURIComponent(text)),
                tracks => tracks.length ? trackTable(tracks) : '<div class="empty">Ничего не найдено</div>');
        };
        q.oninput = () => {
            clearTimeout(wait);
            wait = setTimeout(run, 300);
        };
        q.focus();
        run();
    },

    top() {
        content.innerHTML = '<h1>Популярное</h1><div id="list"></div>';
        section('list', '', api('/api/stream/top?limit=20'), top => playAll + trackTable(topTracks(top), {extra: plays}));
    },

    liked() {
        content.innerHTML = header('liked', 'Любимые треки', 'Плейлист', esc(me.displayName)) + '<div id="list"></div>';
        section('list', '', api('/api/stream/likes/' + me.id), tracks => (tracks.length ? playAll : '') + trackTable(tracks));
    },

    history() {
        content.innerHTML = '<h1>История прослушиваний</h1><div id="list"></div>';
        section('list', '', api('/api/stream/history/' + me.id),
            h => trackTable(h.map(x => ({...x.track, playedAt: x.playedAt})), {extra: {title: 'Когда', value: t => date(t.playedAt)}}));
    },

    async studio() {
        content.innerHTML = header(me.displayName, me.displayName, 'Исполнитель', 'Публикация треков', 'round')
            + `<h2>Новый трек</h2>
            <form id="pubForm" class="form">
                <input name="title" placeholder="Название трека" required maxlength="150">
                <input name="url" placeholder="Ссылка на трек в SoundCloud" required>
                <input name="genre" placeholder="Жанр" value="Хип-хоп" required maxlength="50">
                <button class="primary">Опубликовать</button>
            </form>
            <div id="mine"></div>`;
        $('pubForm').onsubmit = publish;
        await findMyArtist();
        section('mine', 'Мои треки', myArtistId === null ? Promise.resolve([]) : api('/api/tracks').then(t => t.filter(x => x.artistId === myArtistId)),
            tracks => tracks.length ? playAll + trackTable(tracks) : '<div class="empty">Ты пока ничего не опубликовал</div>');
    },

    admin() {
        content.innerHTML = '<h1>Управление</h1><div id="users"></div><div id="all"></div>';
        section('users', 'Пользователи', api('/api/users'), users => `<table class="tracks users">
            <tr><th>Логин</th><th>Имя</th><th>Роль</th><th></th></tr>
            ${users.map(u => `<tr><td>${esc(u.username)}</td><td>${esc(u.displayName)}</td><td><span class="badge ${u.role.toLowerCase()}">${roles[u.role]}</span></td>
                <td>${u.id === me.id ? '' : `<button class="link small" data-delete-user="${u.id}">Удалить</button>`}</td></tr>`).join('')}
        </table>`);
        section('all', 'Все треки', api('/api/tracks'), tracks => trackTable(tracks));
    },

    async artist(id) {
        const a = await api('/api/artists/' + id);
        content.innerHTML = header(a.name, a.name, 'Исполнитель', `${esc(a.genre)}${a.country ? ' · ' + esc(a.country) : ''}`, 'round') + '<div id="tracks"></div><div id="albums"></div>';
        section('tracks', 'Треки', api('/api/tracks').then(t => t.filter(x => x.artistId === id)), tracks => playAll + trackTable(tracks));
        section('albums', 'Синглы и альбомы', api(`/api/artists/${id}/albums`), list => `<div class="grid">${list.map(albumCard).join('')}</div>`);
    },

    async album(id) {
        const a = await api('/api/albums/' + id);
        content.innerHTML = header(a.title, a.title, 'Сингл', `<a data-artist="${a.artist.id}">${esc(a.artist.name)}</a> · ${a.releaseYear}`) + '<div id="tracks"></div>';
        section('tracks', '', api(`/api/albums/${id}/tracks`), tracks => playAll + trackTable(tracks));
    },

    async playlist(id) {
        const p = await api('/api/playlists/' + id);
        const info = `${p.description ? esc(p.description) + '<br>' : ''}Треков: ${p.trackCount} · ${Math.round(p.totalDurationSec / 60)} мин`;
        content.innerHTML = header('pl' + p.name, p.name, 'Плейлист', info)
            + `<div class="actions"><button class="big-play" data-playall>&#9654;</button><button class="link" data-delete-playlist="${p.id}">Удалить плейлист</button></div>`
            + trackTable(p.tracks, {playlistId: p.id});
        if (p.tracks.some(t => t.available === false)) toast('Каталог недоступен, часть треков не загрузилась', true);
    }
};

async function go(page, arg) {
    view = {page, arg};
    lists = {};
    document.querySelectorAll('#nav a').forEach(a => a.classList.toggle('active', a.dataset.page === page));
    document.querySelectorAll('#playlists li').forEach(li => li.classList.toggle('active', page === 'playlist' && Number(li.dataset.id) === arg));
    content.scrollTop = 0;
    try {
        await pages[page](arg);
    } catch (e) {
        content.innerHTML = `<h1>Не удалось открыть</h1><div class="empty">${esc(e.message)}</div>`;
    }
}

const refresh = () => go(view.page, view.arg);

async function loadPlaylists() {
    const ul = $('playlists');
    try {
        myPlaylists = await api('/api/playlists?userId=' + me.id);
        ul.innerHTML = myPlaylists.map(p => `<li data-id="${p.id}"${view.page === 'playlist' && view.arg === p.id ? ' class="active"' : ''}>${cover('pl' + p.name, p.name, 'mini')}<span>${esc(p.name)}</span></li>`).join('')
            || '<li class="muted">Плейлистов пока нет</li>';
    } catch (e) {
        myPlaylists = [];
        ul.innerHTML = `<li class="muted">${esc(e.message)}</li>`;
    }
}

async function loadLikes() {
    try {
        likedIds = new Set((await api('/api/stream/likes/' + me.id)).map(t => t.id));
    } catch (e) {
        likedIds = new Set();
    }
}

async function findMyArtist() {
    myArtistId = null;
    if (me.role !== 'ARTIST') return;
    try {
        const artist = (await api('/api/artists')).find(a => a.userId === me.id);
        myArtistId = artist ? artist.id : null;
    } catch (e) {
        myArtistId = null;
    }
}

function updateHearts(id) {
    document.querySelectorAll(`[data-like="${id}"]`).forEach(b => {
        b.classList.toggle('liked', likedIds.has(id));
        b.innerHTML = heart(id);
    });
}

async function toggleLike(id) {
    try {
        if (likedIds.has(id)) {
            await del(`/api/stream/likes/${me.id}/${id}`);
            likedIds.delete(id);
            toast('Удалено из любимых');
        } else {
            await post('/api/stream/likes', {userId: me.id, trackId: id});
            likedIds.add(id);
            toast('Добавлено в любимые');
        }
        updateHearts(id);
        if (view.page === 'liked') refresh();
    } catch (e) {
        toast(e.message, true);
    }
}

function openModal(title, html) {
    $('modalTitle').textContent = title;
    $('modalBody').innerHTML = html;
    $('modalBody').onclick = null;
    $('modal').classList.remove('hidden');
}

const closeModal = () => $('modal').classList.add('hidden');

function createPlaylist() {
    openModal('Новый плейлист', '<form id="plForm" class="form"><input name="title" placeholder="Название" required maxlength="100"><input name="description" placeholder="Описание" maxlength="255"><button class="primary">Создать</button></form>');
    const form = $('plForm');
    form.elements.title.focus();
    form.onsubmit = async e => {
        e.preventDefault();
        try {
            const p = await post('/api/playlists', {name: form.elements.title.value, description: form.elements.description.value, userId: me.id});
            closeModal();
            toast('Плейлист создан');
            await loadPlaylists();
            go('playlist', p.id);
        } catch (err) {
            toast(err.message, true);
        }
    };
}

function addToPlaylist(trackId) {
    if (!myPlaylists.length) return toast('Сначала создай плейлист', true);
    openModal('Добавить в плейлист', myPlaylists.map(p => `<button class="pick" data-pick="${p.id}">${esc(p.name)}</button>`).join(''));
    $('modalBody').onclick = async e => {
        const id = Number(e.target.dataset.pick);
        if (!id) return;
        try {
            await post(`/api/playlists/${id}/tracks`, {trackId});
            closeModal();
            toast('Трек добавлен');
            if (view.page === 'playlist' && view.arg === id) refresh();
        } catch (err) {
            toast(err.message, true);
        }
    };
}

async function action(question, url, done, reload = true) {
    if (question && !confirm(question)) return false;
    try {
        await del(url);
        toast(done);
        if (reload) refresh();
        return true;
    } catch (e) {
        toast(e.message, true);
        return false;
    }
}

async function deletePlaylist(id) {
    if (!await action('Удалить плейлист?', '/api/playlists/' + id, 'Плейлист удалён', false)) return;
    await loadPlaylists();
    go('home');
}

function probe(url) {
    return new Promise((resolve, reject) => {
        if (!/^https:\/\/(www\.|m\.)?soundcloud\.com\/.+/.test(url)) return reject(new Error('Нужна ссылка на трек в SoundCloud'));
        if (!window.SC) return reject(new Error('Нет связи с SoundCloud'));
        const frame = document.createElement('iframe');
        frame.className = 'probe';
        frame.src = 'https://w.soundcloud.com/player/?url=' + encodeURIComponent(url);
        document.body.appendChild(frame);
        const w = SC.Widget(frame);
        let timeout;
        const finish = (fn, value) => {
            clearTimeout(timeout);
            frame.remove();
            fn(value);
        };
        timeout = setTimeout(() => finish(reject, new Error('Трек не найден на SoundCloud')), 10000);
        w.bind(SC.Widget.Events.READY, () => w.getDuration(ms => ms
            ? finish(resolve, Math.round(ms / 1000))
            : finish(reject, new Error('Трек не найден на SoundCloud'))));
    });
}

async function publish(e) {
    e.preventDefault();
    const f = e.target;
    const button = f.querySelector('button');
    button.disabled = true;
    button.textContent = 'Проверяем ссылку...';
    try {
        const url = f.elements.url.value.trim();
        const durationSec = await probe(url);
        await post('/api/tracks/publish', {
            title: f.elements.title.value.trim(),
            userId: me.id,
            artistName: me.displayName,
            genre: f.elements.genre.value.trim(),
            soundcloudUrl: url,
            durationSec
        });
        toast('Трек опубликован');
        refresh();
    } catch (err) {
        toast(err.message, true);
        button.disabled = false;
        button.textContent = 'Опубликовать';
    }
}

function playList(list, i) {
    queue = list;
    start(i);
}

function setPlaying(value) {
    playing = value;
    $('play').innerHTML = value ? '&#10074;&#10074;' : '&#9654;';
}

function soundcloud(url) {
    if (widget) return widget.load(url, {auto_play: true});
    const frame = document.createElement('iframe');
    frame.id = 'sc';
    frame.allow = 'autoplay';
    frame.src = 'https://w.soundcloud.com/player/?auto_play=true&url=' + encodeURIComponent(url);
    document.body.appendChild(frame);
    widget = SC.Widget(frame);
    widget.bind(SC.Widget.Events.PLAY, () => {
        setPlaying(true);
        widget.getDuration(ms => {
            duration = ms / 1000;
            $('dur').textContent = time(duration);
        });
    });
    widget.bind(SC.Widget.Events.PAUSE, () => setPlaying(false));
    widget.bind(SC.Widget.Events.PLAY_PROGRESS, e => {
        position = e.currentPosition / 1000;
        updateProgress();
    });
    widget.bind(SC.Widget.Events.FINISH, () => next(true));
}

async function start(i) {
    const t = queue[i];
    if (!t) return;
    if (t.available === false || !t.title) return toast('Трек недоступен: каталог не отвечает', true);
    index = i;
    position = 0;
    duration = t.durationSec;
    clearInterval(timer);
    $('nowTitle').textContent = t.title;
    $('nowArtist').textContent = t.artistName || '';
    $('nowCover').style.background = gradient(t.albumTitle || t.title);
    $('nowCover').textContent = String(t.albumTitle || t.title)[0].toUpperCase();
    $('dur').textContent = time(duration);
    $('scLink').href = t.soundcloudUrl || '#';
    $('scLink').hidden = !t.soundcloudUrl;
    $('like').hidden = false;
    $('like').dataset.like = t.id;
    updateHearts(t.id);
    document.title = t.title + ' · SoundHub';
    document.querySelectorAll('tr.row').forEach(r => r.classList.toggle('playing', lists[r.dataset.list][r.dataset.i].id === t.id));
    updateProgress();
    useWidget = !!(t.soundcloudUrl && window.SC);
    if (useWidget) {
        soundcloud(t.soundcloudUrl);
    } else {
        if (widget) widget.pause();
        resume();
    }
    try {
        const r = await post('/api/stream/play', {userId: me.id, trackId: t.id});
        if (r.degraded) toast('Прослушивание сохранено, но один из сервисов недоступен', true);
    } catch (e) {
        toast(e.message, true);
    }
}

function resume() {
    if (index < 0) return;
    if (useWidget) return widget.play();
    clearInterval(timer);
    timer = setInterval(tick, 1000);
    setPlaying(true);
}

function pause() {
    if (index < 0) return;
    if (useWidget) return widget.pause();
    clearInterval(timer);
    setPlaying(false);
}

function tick() {
    position++;
    if (position >= duration) return next(true);
    updateProgress();
}

function updateProgress() {
    $('cur').textContent = time(position);
    $('fill').style.width = (duration ? Math.min(position / duration, 1) * 100 : 0) + '%';
}

function seek(sec) {
    position = sec;
    if (useWidget) widget.seekTo(sec * 1000);
    updateProgress();
}

function next(auto) {
    if (auto === true && repeat === 'one') return start(index);
    if (shuffle && queue.length > 1) {
        let i;
        do i = Math.floor(Math.random() * queue.length); while (i === index);
        return start(i);
    }
    if (index + 1 < queue.length) return start(index + 1);
    if (repeat === 'all') return start(0);
    pause();
    seek(0);
}

function prev() {
    if (position > 3 || index <= 0) seek(0);
    else start(index - 1);
}

function setRepeat(value) {
    repeat = value;
    $('repeat').classList.toggle('on', repeat !== 'off');
    $('repeat').innerHTML = repeat === 'one' ? '&#8635;<sub>1</sub>' : '&#8635;';
    $('repeat').title = {off: 'Повтор выключен', all: 'Повтор плейлиста', one: 'Повтор трека'}[repeat];
}

function resetPlayer() {
    pause();
    queue = [];
    index = -1;
    position = 0;
    duration = 0;
    $('nowTitle').textContent = 'Ничего не играет';
    $('nowArtist').textContent = '';
    $('nowCover').textContent = '';
    $('nowCover').style.background = '';
    $('like').hidden = true;
    $('scLink').hidden = true;
    $('dur').textContent = '0:00';
    updateProgress();
    document.title = 'SoundHub';
}

async function enter(user) {
    me = user;
    localStorage.setItem('me', JSON.stringify(user));
    $('auth').classList.add('hidden');
    $('meName').textContent = me.displayName;
    $('meRole').textContent = roles[me.role];
    $('meCover').style.background = gradient(me.displayName);
    $('meCover').textContent = me.displayName[0].toUpperCase();
    document.querySelectorAll('#nav [data-role]').forEach(a => a.hidden = a.dataset.role !== me.role);
    await Promise.all([loadLikes(), findMyArtist()]);
    loadPlaylists();
    go('home');
}

function logout() {
    resetPlayer();
    me = null;
    localStorage.removeItem('me');
    content.innerHTML = '';
    $('playlists').innerHTML = '';
    $('loginForm').reset();
    $('auth').classList.remove('hidden');
}

$('loginForm').onsubmit = async e => {
    e.preventDefault();
    const f = e.target;
    try {
        enter(await post('/api/users/login', {username: f.elements.username.value.trim(), password: f.elements.password.value}));
    } catch (err) {
        toast(err.message, true);
    }
};

$('registerForm').onsubmit = async e => {
    e.preventDefault();
    const f = e.target;
    try {
        enter(await post('/api/users', {
            username: f.elements.username.value.trim(),
            password: f.elements.password.value,
            displayName: f.elements.displayName.value.trim(),
            role: f.elements.role.value
        }));
        toast('Аккаунт создан');
    } catch (err) {
        toast(err.message, true);
    }
};

document.querySelectorAll('[data-switch]').forEach(b => b.onclick = () => {
    $('loginForm').hidden = !$('loginForm').hidden;
    $('registerForm').hidden = !$('registerForm').hidden;
});

$('nav').onclick = e => {
    const a = e.target.closest('a');
    if (a) go(a.dataset.page);
};

$('playlists').onclick = e => {
    const li = e.target.closest('li[data-id]');
    if (li) go('playlist', Number(li.dataset.id));
};

content.onclick = e => {
    const el = e.target.closest('[data-like],[data-add],[data-remove],[data-delete-track],[data-delete-user],[data-artist],[data-album],[data-playall],[data-delete-playlist],tr.row');
    if (!el) return;
    const d = el.dataset;
    if (d.like) toggleLike(Number(d.like));
    else if (d.add) addToPlaylist(Number(d.add));
    else if (d.remove) action(null, `/api/playlists/${view.arg}/tracks/${d.remove}`, 'Трек убран из плейлиста');
    else if (d.deleteTrack) action('Удалить трек?', '/api/tracks/' + d.deleteTrack, 'Трек удалён');
    else if (d.deleteUser) action('Удалить пользователя?', '/api/users/' + d.deleteUser, 'Пользователь удалён');
    else if (d.artist) go('artist', Number(d.artist));
    else if (d.album) go('album', Number(d.album));
    else if (d.deletePlaylist) deletePlaylist(Number(d.deletePlaylist));
    else if ('playall' in d) {
        const row = content.querySelector('tr.row');
        if (row) playList(lists[row.dataset.list], 0);
    } else if (d.list) playList(lists[d.list], Number(d.i));
};

$('logout').onclick = logout;
$('newPlaylist').onclick = createPlaylist;
$('modalClose').onclick = closeModal;
$('modal').onclick = e => e.target === $('modal') && closeModal();
$('play').onclick = () => playing ? pause() : resume();
$('next').onclick = () => next();
$('prev').onclick = prev;
$('like').onclick = () => toggleLike(Number($('like').dataset.like));
$('repeat').onclick = () => setRepeat({off: 'all', all: 'one', one: 'off'}[repeat]);

$('shuffle').onclick = () => {
    shuffle = !shuffle;
    $('shuffle').classList.toggle('on', shuffle);
    $('shuffle').title = shuffle ? 'Перемешивание включено' : 'Перемешать';
};

$('bar').onclick = e => {
    if (index < 0) return;
    const r = $('bar').getBoundingClientRect();
    seek(Math.floor((e.clientX - r.left) / r.width * duration));
};

document.onkeydown = e => {
    if (me && e.code === 'Space' && !['INPUT', 'SELECT', 'TEXTAREA'].includes(e.target.tagName)) {
        e.preventDefault();
        playing ? pause() : resume();
    }
};

const saved = JSON.parse(localStorage.getItem('me') || 'null');
if (saved) {
    api('/api/users/' + saved.id).then(enter).catch(e => e.status === 404 ? logout() : enter(saved));
} else {
    logout();
}
