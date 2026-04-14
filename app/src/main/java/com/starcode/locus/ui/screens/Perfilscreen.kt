package com.starcode.locus.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.starcode.locus.R
import com.starcode.locus.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onLogoutNavigation: () -> Unit
) {
    val usuario by authViewModel.usuarioLogueado.collectAsState()
    var modoEdicion by remember { mutableStateOf(false) }

    var nombreEdit by remember { mutableStateOf("") }
    var passEdit by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }

    LaunchedEffect(usuario) {
        usuario?.let { nombreEdit = it.nombre }
    }

    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)

    if (usuario == null) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.doggy))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
        Box(
            modifier = Modifier.fillMaxSize().background(LocusBackground),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(200.dp))
        }
    } else {
        Scaffold(
            containerColor = LocusBackground,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("MI PERFIL", fontWeight = FontWeight.Black, color = LocusDeepPurple, letterSpacing = 1.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = LocusDeepPurple)
                        }
                    },
                    actions = {
                        TextButton(onClick = { modoEdicion = !modoEdicion }) {
                            Text(
                                text = if (modoEdicion) "Cancelar" else "Editar",
                                color = LocusActionOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .windowInsetsPadding(WindowInsets.ime)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- AVATAR CON SOMBRA ---
                val avatarRes = when (usuario?.genero?.lowercase()) {
                    "femenino", "mujer" -> R.drawable.girl1
                    "masculino", "hombre" -> R.drawable.boy1
                    else -> R.drawable.boy2
                }

                Box(
                    modifier = Modifier
                        .size(140.dp)
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

                Text(
                    text = if (modoEdicion) "Configuración de Perfil" else "${usuario?.nombre} ${usuario?.ape_pa}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LocusDeepPurple
                )

                Text(
                    text = usuario?.email ?: "",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(40.dp))

                // --- TARJETA DE INFORMACIÓN AGRUPADA ---
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

                        // Nombre
                        ProfileItem(
                            label = "Nombre de usuario",
                            value = nombreEdit,
                            isEditing = modoEdicion,
                            onValueChange = { nombreEdit = it },
                            icon = Icons.Default.Person
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = LocusBackground, thickness = 1.dp)

                        // Correo (Siempre lectura)
                        ProfileItem(
                            label = "Correo Electrónico",
                            value = usuario?.email ?: "",
                            isEditing = false,
                            onValueChange = {},
                            icon = Icons.Default.Email
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = LocusBackground, thickness = 1.dp)

                        // Contraseña
                        ProfileItem(
                            label = "Contraseña",
                            value = passEdit,
                            isEditing = modoEdicion,
                            onValueChange = { passEdit = it },
                            icon = Icons.Default.Lock,
                            isPassword = true,
                            passwordVisible = passVisible,
                            onPasswordToggle = { passVisible = !passVisible }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (modoEdicion) {
                    Button(
                        onClick = { modoEdicion = false },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Guardar cambios", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                } else {
                    TextButton(
                        onClick = {
                            authViewModel.cerrarSesion()
                            onLogoutNavigation()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
                        Spacer(Modifier.width(12.dp))
                        Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileItem(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFFFDF6EE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFFE6673D), modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

            if (isEditing) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().offset(x = (-16).dp),
                    visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFFE6673D),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    placeholder = { Text(if(isPassword) "••••••••" else "Escribe aquí...", color = Color.LightGray) },
                    singleLine = true,
                    trailingIcon = if (isPassword) {
                        {
                            IconButton(onClick = onPasswordToggle) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFFE6673D)
                                )
                            }
                        }
                    } else null
                )
            } else {
                Text(
                    text = if (isPassword) "••••••••" else value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
            }
        }
    }
}