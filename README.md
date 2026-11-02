# Scarlet Inspector

**WebSocket inspection for [Scarlet](https://github.com/Tinder/Scarlet) on Android.** It records every
WebSocket frame into Room and exposes sessions in a Compose inspector launched from a persistent
notification — because logcat has no concept of a session.

HTTP inspectors like Chucker stop at request/response. Long-lived sockets are a different problem:
frames fly past, reconnects blur together, and there is nothing to hold onto afterwards. This
library mirrors the decorator idea onto Scarlet.

## How it works

