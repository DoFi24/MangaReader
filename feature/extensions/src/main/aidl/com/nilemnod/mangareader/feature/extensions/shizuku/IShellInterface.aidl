package com.nilemnod.mangareader.feature.extensions.shizuku;

interface IShellInterface {
    void install(in AssetFileDescriptor apk, in IntentSender intentSender, String packageName) = 1;

    void destroy() = 16777114;
}
