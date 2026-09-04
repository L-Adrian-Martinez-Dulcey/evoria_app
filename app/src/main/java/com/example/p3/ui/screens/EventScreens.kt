@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.p3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.p3.data.model.Event
import com.example.p3.data.model.User
import com.example.p3.ui.viewmodel.EventViewModel
import com.example.p3.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EventHomeScreen(viewModel: EventViewModel, navController: NavController) {
    val state by viewModel.uiState.collectAsState()
    EventFeedback(state.error, state.message) { viewModel.clearMessage() }
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { navController.navigate("event_form") }) { Icon(Icons.Default.Add, "Crear evento") } }) { padding ->
        when {
            state.isLoading && state.events.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.events.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("No hay eventos disponibles") }
            else -> LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(state.events, key = { index, event -> event.id ?: "event-$index" }) { _, event ->
                    EventCard(event) {
                        event.id?.let { navController.navigate("event_detail/${Uri.encode(it)}") }
                    }
                }
            }
        }
    }
}

@Composable private fun EventCard(event: Event, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            if (event.coverImage.isNotBlank()) AsyncImage(
                event.coverImage,
                null,
                Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )
            Text(event.title, style = MaterialTheme.typography.titleLarge)
            Text("${event.date} · ${event.time}")
            Text("${event.category} · ${event.place}")
            Text("${event.availableSlots} cupos disponibles", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun EventDetailScreen(eventId: String, user: User, viewModel: EventViewModel, navController: NavController) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }
    val event = state.events.firstOrNull { it.id == eventId }
    var confirmDelete by remember { mutableStateOf(false) }
    var showReview by remember { mutableStateOf(false) }
    if (event == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.error != null) Text(state.error!!)
            else CircularProgressIndicator()
        }
        return
    }
    val isCreator = event.creatorId == user.id
    val isRegistered = event.registrations.any { it.userId == user.id }
    Scaffold(topBar = { TopAppBar(title = { Text("Detalle del evento") }, navigationIcon = { IconButton({ navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                if (event.coverImage.isNotBlank()) AsyncImage(
                    event.coverImage,
                    null,
                    Modifier.fillMaxWidth().aspectRatio(1f),
                    contentScale = ContentScale.Crop,
                )
                Text(event.title, style = MaterialTheme.typography.headlineMedium)
                Text(event.description)
                DetailLine("Fecha", event.date); DetailLine("Hora", event.time); DetailLine("Lugar", event.place); DetailLine("Categoría", event.category); DetailLine("Cupos", event.availableSlots.toString())
                if (isCreator) Row {
                    OutlinedButton({ navController.navigate("event_form/${event.id}") }) { Icon(Icons.Default.Edit, null); Text(" Editar") }
                    Spacer(Modifier.width(8.dp)); OutlinedButton({ confirmDelete = true }) { Icon(Icons.Default.Delete, null); Text(" Eliminar") }
                } else {
                    Button({ viewModel.register(event, user.id.orEmpty()) }, enabled = !isRegistered && event.availableSlots > 0, modifier = Modifier.fillMaxWidth()) { Text(if (isRegistered) "Ya estás inscrito" else "Inscribirme") }
                    if (isRegistered && hasFinished(event.date)) OutlinedButton({ showReview = true }, Modifier.fillMaxWidth()) { Text("Calificar evento") }
                }
                if (event.reviews.isNotEmpty()) { Text("Reseñas", style = MaterialTheme.typography.titleMedium); event.reviews.forEach { Text("${it.rating}/5 · ${it.comment}") } }
            }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("¿Eliminar evento?") }, text = { Text("Esta acción no se puede deshacer.") }, confirmButton = { TextButton({ viewModel.delete(event) { navController.popBackStack() } }) { Text("Eliminar") } }, dismissButton = { TextButton({ confirmDelete = false }) { Text("Cancelar") } })
    if (showReview) ReviewDialog(onDismiss = { showReview = false }) { rating, comment -> viewModel.addReview(event, user.id.orEmpty(), rating, comment); showReview = false }
}

@Composable private fun DetailLine(label: String, value: String) { Text("$label: $value") }

@Composable
fun EventFormScreen(eventId: String?, user: User, viewModel: EventViewModel, navController: NavController) {
    val state by viewModel.uiState.collectAsState(); val existing = state.events.firstOrNull { it.id == eventId }
    val canEdit = existing == null || existing.creatorId == user.id
    val context = LocalContext.current
    var title by remember(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }; var description by remember(existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var date by remember(existing?.id) { mutableStateOf(existing?.date.orEmpty()) }; var time by remember(existing?.id) { mutableStateOf(existing?.time.orEmpty()) }
    var place by remember(existing?.id) { mutableStateOf(existing?.place.orEmpty()) }; var category by remember(existing?.id) { mutableStateOf(existing?.category.orEmpty()) }
    var slots by remember(existing?.id) { mutableStateOf(existing?.availableSlots?.toString() ?: "") }; var image by remember(existing?.id) { mutableStateOf(existing?.coverImage.orEmpty()) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { selected ->
        runCatching { context.contentResolver.takePersistableUriPermission(selected, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        image = selected.toString()
    } }
    if (eventId != null && existing != null && !canEdit) {
        LaunchedEffect(eventId) { navController.popBackStack() }
        return
    }
    Scaffold(topBar = { TopAppBar(title = { Text(if (existing == null) "Crear evento" else "Editar evento") }, navigationIcon = { IconButton({ navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            AppField(title, { title = it }, "Título"); AppField(description, { description = it }, "Descripción", single = false)
            DateField(date) { date = it }; TimeField(time) { time = it }
            AppField(place, { place = it }, "Lugar"); CategoryField(category) { category = it }; AppField(slots, { slots = it }, "Cupos disponibles", KeyboardType.Number)
            OutlinedButton({ imagePicker.launch(arrayOf("image/*")) }, Modifier.fillMaxWidth()) { Text(if (image.isBlank()) "Seleccionar imagen del evento" else "Cambiar imagen del evento") }
            if (image.isNotBlank()) AsyncImage(
                image,
                "Imagen del evento",
                Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )
            Button(onClick = { viewModel.save(Event(existing?.id, existing?.creatorId ?: user.id.orEmpty(), title.trim(), description.trim(), date.trim(), time.trim(), place.trim(), category.trim(), slots.toIntOrNull() ?: -1, image.trim(), existing?.createdAt ?: now(), existing?.registrations ?: emptyList(), existing?.reviews ?: emptyList())) { navController.popBackStack() } }, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }
        }
    }
}

@Composable private fun AppField(value: String, change: (String) -> Unit, label: String, keyboard: KeyboardType = KeyboardType.Text, single: Boolean = true, readOnly: Boolean = false) {
    OutlinedTextField(
        value,
        change,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = single,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
    )
}

@Composable private fun DateField(value: String, onValue: (String) -> Unit) { val context = LocalContext.current; OutlinedButton(onClick = { val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }; DatePickerDialog(context, { _, y, m, d -> onValue(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).apply { datePicker.minDate = c.timeInMillis }.show() }, modifier = Modifier.fillMaxWidth()) { Text(if (value.isBlank()) "Seleccionar fecha" else "Fecha: $value") } }
@Composable private fun TimeField(value: String, onValue: (String) -> Unit) { val context = LocalContext.current; OutlinedButton(onClick = { val c = Calendar.getInstance(); TimePickerDialog(context, { _, h, m -> onValue(String.format(Locale.US, "%02d:%02d", h, m)) }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show() }, modifier = Modifier.fillMaxWidth()) { Text(if (value.isBlank()) "Seleccionar hora" else "Hora: $value") } }
@Composable private fun CategoryField(value: String, onValue: (String) -> Unit) { var expanded by remember { mutableStateOf(false) }; val options = listOf("Arte", "Deporte", "Tecnología", "Música", "Gastronomía", "Educación", "Otra"); ExposedDropdownMenuBox(expanded, { expanded = it }) { OutlinedTextField(value, {}, readOnly = true, label = { Text("Categoría") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth()); ExposedDropdownMenu(expanded, { expanded = false }) { options.forEach { option -> DropdownMenuItem({ Text(option) }, { onValue(if (option == "Otra") "" else option); expanded = false }) } } }; if (value.isBlank()) AppField(value, onValue, "Escribe otra categoría") }

@Composable
fun MyEventsScreen(user: User, viewModel: EventViewModel, navController: NavController) {
    val events by viewModel.uiState.collectAsState(); var tab by remember { mutableStateOf(0) }
    val list = if (tab == 0) events.events.filter { it.creatorId == user.id } else events.events.filter { event -> event.registrations.any { it.userId == user.id } }
    Column { TabRow(tab) { listOf("Creados", "Inscritos").forEachIndexed { index, text -> Tab(selected = tab == index, onClick = { tab = index }, text = { Text(text) }) } }; LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { itemsIndexed(list, key = { index, event -> event.id ?: "my-event-$index" }) { _, event -> EventCard(event) { event.id?.let { navController.navigate("event_detail/$it") } } } } }
}

@Composable
fun ProfileScreen(user: User, userViewModel: UserViewModel, navController: NavController) {
    val context = LocalContext.current
    val profileError by userViewModel.error.collectAsState()
    var editing by remember { mutableStateOf(false) }; var name by remember(user.id) { mutableStateOf(user.name) }; var email by remember(user.id) { mutableStateOf(user.email) }; var phone by remember(user.id) { mutableStateOf(user.phone) }; var city by remember(user.id) { mutableStateOf(user.city) }; var avatar by remember(user.id) { mutableStateOf(user.avatar.orEmpty()) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { selected -> runCatching { context.contentResolver.takePersistableUriPermission(selected, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; avatar = selected.toString() } }
    Scaffold(topBar = { TopAppBar(title = { Text("Mi perfil") }, navigationIcon = { IconButton({ navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }) }) { padding -> Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        profileError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (avatar.isNotBlank()) AsyncImage(
            avatar,
            "Foto de perfil",
            Modifier
                .size(120.dp)
                .aspectRatio(1f)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        if (editing) OutlinedButton({ imagePicker.launch(arrayOf("image/*")) }) { Text("Seleccionar foto") }
        AppField(name, { name = it }, "Nombre", readOnly = !editing)
        AppField(email, { email = it }, "Email", readOnly = !editing)
        AppField(phone, { phone = it }, "Teléfono", readOnly = !editing)
        AppField(city, { city = it }, "Ciudad", readOnly = !editing)
        if (editing) {
            Button({ userViewModel.updateProfile(user.copy(name = name, email = email, phone = phone, city = city, avatar = avatar)) { editing = false } }, Modifier.fillMaxWidth()) { Text("Guardar perfil") }
            TextButton({ editing = false; name = user.name; email = user.email; phone = user.phone; city = user.city; avatar = user.avatar.orEmpty() }, Modifier.fillMaxWidth()) { Text("Cancelar") }
        } else {
            Button({ editing = true }, Modifier.fillMaxWidth()) { Text("Actualizar perfil") }
        }
        TextButton({ userViewModel.logout { navController.navigate("login") { popUpTo("home") { inclusive = true } } } }, Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    } }
}

@Composable private fun EventFeedback(error: String?, message: String?, clear: () -> Unit) { if (error != null || message != null) LaunchedEffect(error, message) { /* El estado se visualiza en cada pantalla sin ocultar errores. */ } }
@Composable private fun ReviewDialog(onDismiss: () -> Unit, save: (Int, String) -> Unit) { var rating by remember { mutableStateOf("") }; var comment by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Calificar evento") }, text = { Column { AppField(rating, { rating = it }, "Puntaje (1-5)", KeyboardType.Number); AppField(comment, { comment = it }, "Comentario", single = false) } }, confirmButton = { TextButton({ save(rating.toIntOrNull() ?: 0, comment) }) { Text("Publicar") } }, dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }) }
private fun now() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Calendar.getInstance().time)
private fun hasFinished(date: String) = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)?.before(Calendar.getInstance().time) == true }.getOrDefault(false)
