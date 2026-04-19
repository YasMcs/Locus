package com.starcode.locusapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starcode.locusapp.ui.components.LocusDoggyState
import com.starcode.locusapp.ui.viewmodels.AuthViewModel
import com.starcode.locusapp.ui.viewmodels.MapaViewModel
import com.starcode.locusapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    authViewModel: AuthViewModel,
    mapaViewModel: MapaViewModel, 
    onNavigateToMapa: () -> Unit,
    onNavigateToRecuerdos: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onNavigateToEstadisticas: () -> Unit,
    onLogoutNavigation: () -> Unit
) {
    val usuario by authViewModel.usuarioLogueado.collectAsState()

    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)

    Scaffold(
        containerColor = LocusBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    shape = RoundedCornerShape(36.dp),
                    color = LocusSurfaceWhite,
                    shadowElevation = 15.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavBarItem(
                            icon = Icons.Outlined.FavoriteBorder,
                            iconSelected = Icons.Rounded.Favorite,
                            label = "Favoritos",
                            color = LocusActionOrange,
                            onClick = onNavigateToFavoritos
                        )
                        NavBarItem(
                            icon = Icons.Outlined.PhotoLibrary,
                            iconSelected = Icons.Rounded.PhotoLibrary,
                            label = "Recuerdos",
                            color = LocusActionOrange,
                            onClick = onNavigateToRecuerdos
                        )
                        NavBarItem(
                            icon = Icons.Outlined.Map,
                            iconSelected = Icons.Rounded.Map,
                            label = "Explorar",
                            color = LocusActionOrange,
                            onClick = onNavigateToMapa
                        )
                        NavBarItem(
                            icon = Icons.Outlined.EmojiEvents,
                            iconSelected = Icons.Rounded.EmojiEvents,
                            label = "Logros",
                            color = LocusActionOrange,
                            onClick = onNavigateToEstadisticas
                        )
                        NavBarItem(
                            icon = Icons.Outlined.Person,
                            iconSelected = Icons.Rounded.Person,
                            label = "Perfil",
                            color = LocusActionOrange,
                            isSelected = true,
                            onClick = { /* Ya estamos aquí */ }
                        )
                    }
                }
            }
        }
    ) { padding ->
        // ✅ La lógica de carga ahora está DENTRO del Scaffold
        if (usuario == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                LocusDoggyState(message = "Cargando perfil...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- AVATAR ---
                val avatarRes = when (usuario?.genero?.lowercase()) {
                    "femenino", "mujer" -> R.drawable.girl1
                    "masculino", "hombre" -> R.drawable.boy1
                    else -> R.drawable.boy2
                }

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.CenterHorizontally)
                        .shadow(12.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${usuario?.nombre} ${usuario?.ape_pa}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LocusDeepPurple
                    )

                    Text(
                        text = usuario?.email ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Tus Datos Locus",
                            fontWeight = FontWeight.Bold,
                            color = LocusActionOrange,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        ProfileItem(
                            label = "Nombre de usuario",
                            value = usuario?.nombre ?: "",
                            icon = Icons.Outlined.Person
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = LocusBackground, thickness = 1.dp)

                        ProfileItem(
                            label = "Correo Electrónico",
                            value = usuario?.email ?: "",
                            icon = Icons.Outlined.Email
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = LocusBackground, thickness = 1.dp)

                        ProfileItem(
                            label = "Género",
                            value = usuario?.genero ?: "No especificado",
                            icon = Icons.Outlined.Face
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(
                    onClick = {
                        authViewModel.cerrarSesion()
                        onLogoutNavigation()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Outlined.Logout, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(12.dp))
                    Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ProfileItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFFE6673D).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFFE6673D), modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
            )
        }
    }
}