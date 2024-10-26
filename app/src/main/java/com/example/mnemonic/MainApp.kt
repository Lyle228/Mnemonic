package com.example.mnemonic

import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mnemonic.chatgpt.viewmodel.ChatGPTViewModel
import com.example.mnemonic.screens.CalendarScreen
import com.example.mnemonic.screens.HomeScreen
import com.example.mnemonic.screens.ProfileScreen
import com.example.mnemonic.ui.theme.MnemonicTheme
import com.example.mnemonic.weather.api.RetrofitInstance
import com.example.mnemonic.weather.model.PrecipitationType
import com.example.mnemonic.weather.model.WeatherType
import com.example.mnemonic.weather.repository.WeatherRepository
import com.example.mnemonic.weather.viewmodel.WeatherViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
/* TODO viewmodel 전달 방법 개선, 전환 애니메이션 없애기*/
@Composable
fun MainApp (
    modifier: Modifier = Modifier,
    weatherViewModel: WeatherViewModel = viewModel(),
    chatGPTViewModel: ChatGPTViewModel = viewModel()
) {
    val navController = rememberNavController()
    Scaffold (
        modifier = Modifier,
        bottomBar = {
            BottomNavigation(
                modifier = Modifier,
                navController = navController
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(235, 239, 240, 255))
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = modifier
                    .padding(it)
            ) {
                MyNavHost(
                    modifier = Modifier,
                    navController = navController,
                    weatherViewModel = weatherViewModel,
                    chatGPTViewModel = chatGPTViewModel
                )
            }
        }
    }
}

sealed class BottomNavItem(
    @StringRes val title: Int,
    val icon: ImageVector,
    val screenRoute: String
) {
    data object Home : BottomNavItem(R.string.home, Icons.Rounded.Home, "a")
    data object Calendar : BottomNavItem(R.string.calendar, Icons.Rounded.DateRange, "b")
    data object Profile : BottomNavItem(R.string.profile, Icons.Rounded.AccountCircle, "c")
}

@Composable
fun BottomNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = listOf<BottomNavItem>(
        BottomNavItem.Home,
        BottomNavItem.Calendar,
        BottomNavItem.Profile
    )

    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = Color.Black,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screenRoute,
                label = {
                    Text(
                        text = stringResource(id = item.title),
                        style = TextStyle(
                            fontSize = 12.sp
                        )
                    )
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(id = item.title)
                    )
                },
                onClick = {
                    navController.navigate(item.screenRoute) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) { saveState = true }
                        }
                        launchSingleTop = true // 백스택에 중복으로 쌓이지 않게
                        restoreState = true
                    }
                },
            )
        }
    }
}
@Composable
fun MyNavHost(
    modifier: Modifier,
    navController: NavHostController,
    weatherViewModel: WeatherViewModel,
    chatGPTViewModel: ChatGPTViewModel
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomNavItem.Home.screenRoute
    ) {
        composable(BottomNavItem.Home.screenRoute) {
            HomeScreen(modifier = Modifier, weatherViewModel = weatherViewModel, chatGPTViewModel = chatGPTViewModel)
        }
        composable(BottomNavItem.Calendar.screenRoute) {
            CalendarScreen()
        }
        composable(BottomNavItem.Profile.screenRoute) {
            ProfileScreen()
        }
    }
}