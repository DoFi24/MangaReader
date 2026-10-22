package com.nilemnod.mangareader.app.shizuku;

interface IShellInterface {
    void install(in AssetFileDescriptor apk, in IntentSender intentSender) = 1;

    void destroy() = 16777114;
}
