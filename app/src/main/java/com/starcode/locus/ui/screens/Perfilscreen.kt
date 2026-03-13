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

    // Estados de edición
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
        Box(modifier = Modifier.fillMaxSize().background(LocusBackground), contentAlignment = Alignment.Center) {
            LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(200.dp))
        }
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Mi Perfil", fontWeight = FontWeight.Black, color = LocusDeepPurple) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = LocusDeepPurple)
                        }
                    },
                    actions = {
                        // Botón con texto para dar contexto claro
                        TextButton(onClick = { modoEdicion = !modoEdicion }) {
                            Text(
                                text = if (modoEdicion) "Cancelar" else "Editar",
                                color = LocusActionOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                    modifier = Modifier.shadow(4.dp)
                )
            },
            containerColor = LocusBackground
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar dinámico
                val avatarRes = when (usuario?.genero?.lowercase()) {
                    "femenino", "mujer" -> R.drawable.girl1
                    "masculino", "hombre" -> R.drawable.boy1
                    else -> R.drawable.boy2
                }

                Image(
                    painter = painterResource(id = avatarRes),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.White).padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (modoEdicion) "Editando Información" else "${usuario?.nombre} ${usuario?.ape_pa}",
                    fontSize = if (modoEdicion) 16.sp else 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (modoEdicion) LocusActionOrange else LocusDeepPurple
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- CAMPOS UNIFICADOS ---

                // Nombre
                EditableInfoCard(
                    label = "Nombre",
                    value = nombreEdit,
                    isEditing = modoEdicion,
                    onValueChange = { nombreEdit = it },
                    icon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email (Solo lectura por seguridad usualmente)
                InfoCard(label = "Correo Electrónico", value = usuario?.email ?: "", icon = Icons.Default.Email)

                Spacer(modifier = Modifier.height(12.dp))

                // Contraseña (Mismo estilo que las demás)
                EditableInfoCard(
                    label = "Contraseña",
                    value = passEdit,
                    isEditing = modoEdicion,
                    onValueChange = { passEdit = it },
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passVisible,
                    onPasswordToggle = { passVisible = !passVisible }
                )

                if (modoEdicion) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { modoEdicion = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    // EL BOTÓN DE CERRAR SESIÓN SOLO SE VE SI NO ESTÁS EDITANDO
                    Spacer(modifier = Modifier.height(48.dp))
                    TextButton(
                        onClick = {
                            authViewModel.cerrarSesion()
                            onLogoutNavigation()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditableInfoCard(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = if (isEditing) 4.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color(0xFFE6673D), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Etiqueta (Label): Un gris medio para que se lea pero no distraiga
                Text(label, fontSize = 11.sp, color = Color.DarkGray)

                if (isEditing) {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = (-16).dp),
                        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                        // Ajuste de colores para legibilidad máxima
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20), // Negro Locus
                            unfocusedTextColor = Color(0xFF1D1B20),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = Color(0xFFE6673D),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        placeholder = {
                            Text(
                                text = if(isPassword) "Escribe nueva contraseña" else "Escribe tu nombre",
                                fontSize = 16.sp,
                                color = Color.Gray // Gris visible
                            )
                        },
                        singleLine = true,
                        trailingIcon = if (isPassword) {
                            {
                                IconButton(onClick = onPasswordToggle) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color(0xFFE6673D) // Naranja para que resalte el botón
                                    )
                                }
                            }
                        } else null
                    )
                } else {
                    // Texto en modo lectura: Negro fuerte
                    Text(
                        text = if (isPassword) "••••••••" else value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20) // Color de alto contraste
                    )
                }
            }
        }
    }
}
@Composable
fun InfoCard(label: String, value: String, icon: ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFFE6673D), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, fontSize = 11.sp, color = Color.Gray)
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
            }
        }
    }
}