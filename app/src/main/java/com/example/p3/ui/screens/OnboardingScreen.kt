package com.example.p3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Navy = Color(0xFF2F4156)
private val Teal = Color(0xFF567C8D)
private val Beige = Color(0xFFF5EFE6)
private val SecondaryText = Color(0xFF7A8F9E)

data class OnboardingPage(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {

    val pages = listOf(

        OnboardingPage(
            icon = Icons.Default.Event,
            title = "Bienvenido a EVORIA",
            description = "Una plataforma diseñada para centralizar la planificación y gestión de tus eventos en un solo lugar."
        ),

        OnboardingPage(
            icon = Icons.Default.Inventory2,
            title = "Todo bajo control",
            description = "Gestiona eventos, recursos, inventario, presupuestos y cronogramas de forma organizada y eficiente."
        ),

        OnboardingPage(
            icon = Icons.Default.TrendingUp,
            title = "Planifica. Organiza. Logra.",
            description = "Esperamos que EVORIA haga que la planificación de tus eventos sea más sencilla, rápida y confiable."
        )
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Navy,
                        Teal
                    )
                )
            )
    ) {

        // Botón Omitir
        if (pagerState.currentPage < pages.lastIndex) {

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 30.dp,
                        end = 20.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Omitir",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(5.dp))

                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Omitir",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Área clickeable para Omitir
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 15.dp, end = 10.dp)
                    .size(width = 100.dp, height = 50.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onFinish() }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 75.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 30.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo
            Text(
                text = "EVORIA",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(25.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->

                OnboardingPageContent(
                    page = pages[page]
                )
            }

            // Indicadores
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                pages.indices.forEach { index ->

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .size(
                                width = if (pagerState.currentPage == index) 25.dp else 9.dp,
                                height = 9.dp
                            )
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    Beige
                                else
                                    Color.White.copy(alpha = 0.35f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Botón
            Button(
                onClick = {

                    if (pagerState.currentPage == pages.lastIndex) {

                        onFinish()

                    } else {

                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Beige,
                    contentColor = Navy
                )
            ) {

                if (pagerState.currentPage == pages.lastIndex) {

                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Registrarse",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                } else {

                    Text(
                        text = "Siguiente",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Círculo del icono
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Beige,
                modifier = Modifier.size(70.dp)
            )
        }

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = page.title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = page.description,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 17.sp,
            lineHeight = 27.sp,
            textAlign = TextAlign.Center
        )
    }
}