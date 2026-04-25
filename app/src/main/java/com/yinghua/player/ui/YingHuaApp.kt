package com.yinghua.player.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yinghua.player.ui.folder.FolderScreen
import com.yinghua.player.ui.home.HomeScreen
import com.yinghua.player.ui.network.NetworkScreen
import com.yinghua.player.ui.player.PlayerScreen
import com.yinghua.player.ui.search.SearchScreen
import com.yinghua.player.ui.settings.SettingsScreen
import com.yinghua.player.ui.theme.YingHuaTheme

@Composable
fun YingHuaApp(externalVideoPath: String? = null) {
    YingHuaTheme {
        val navController = rememberNavController()

        // If launched from external intent, go directly to player
        val startDestination = if (externalVideoPath != null) {
            Screen.Player.createRoute(externalVideoPath)
        } else {
            Screen.Home.route
        }

        NavHost(navController = navController, startDestination = startDestination) {

            composable(Screen.Home.route) {
                HomeScreen(
                    onFolderClick = { folderPath ->
                        navController.navigate(Screen.Folder.createRoute(folderPath))
                    },
                    onSearchClick = { navController.navigate(Screen.Search.route) },
                    onNetworkClick = { navController.navigate(Screen.Network.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                )
            }

            composable(
                route = Screen.Folder.route,
                arguments = listOf(navArgument("folderPath") { type = NavType.StringType }),
            ) { backStack ->
                val encoded = backStack.arguments?.getString("folderPath") ?: ""
                val folderPath = Uri.decode(encoded)
                FolderScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { videoPath ->
                        navController.navigate(Screen.Player.createRoute(videoPath))
                    },
                )
            }

            composable(
                route = Screen.Player.route,
                arguments = listOf(navArgument("videoPath") { type = NavType.StringType }),
            ) {
                PlayerScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Network.route) {
                NetworkScreen(
                    onBack = { navController.popBackStack() },
                    onPlayUrl = { url ->
                        navController.navigate(Screen.Player.createRoute(url))
                    },
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onScan = {
                        navController.popBackStack()
                        // HomeViewModel will handle scan trigger from home
                    },
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { videoPath ->
                        navController.navigate(Screen.Player.createRoute(videoPath))
                    },
                )
            }
        }
    }
}
