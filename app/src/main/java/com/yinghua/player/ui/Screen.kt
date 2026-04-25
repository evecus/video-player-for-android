package com.yinghua.player.ui

import android.net.Uri

sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object Folder   : Screen("folder/{folderPath}") {
        fun createRoute(folderPath: String): String =
            "folder/${Uri.encode(folderPath)}"
    }
    object Player   : Screen("player/{videoPath}") {
        fun createRoute(videoPath: String): String =
            "player/${Uri.encode(videoPath)}"
    }
    object Network  : Screen("network")
    object Settings : Screen("settings")
    object Search   : Screen("search")
}
