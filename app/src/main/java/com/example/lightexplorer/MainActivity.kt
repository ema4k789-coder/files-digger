package com.example.lightexplorer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.os.Handler
import android.os.Looper
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.webkit.MimeTypeMap
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.media.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import android.widget.MediaController
import android.widget.VideoView
import android.widget.Toast
import android.view.TextureView
import android.view.Surface
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import androidx.documentfile.provider.DocumentFile

private data class FileItem(
    val file: File,
    val name: String,
    val isDirectory: Boolean,
    val size: String,
    val modified: String
)

private enum class FileCategory(val label: String) {
    ALL("Todos"),
    APKS("APK"),
    IMAGES("Imágenes"),
    VIDEOS("Videos"),
    AUDIO("Audio"),
    DOCUMENTS("Documentos"),
    ARCHIVES("Archivos"),
    OTHERS("Otros")
}

private enum class SortMode(val label: String) {
    NAME("Nombre"),
    SIZE("Tamaño"),
    MODIFIED("Modificados"),
    TYPE("Tipo")
}

private enum class TransferMode {
    COPY,
    MOVE
}

@Composable
private fun ElegantExplorerTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = Color(0xFFECA45B),
        onPrimary = Color(0xFF24130D),
        primaryContainer = Color(0xFF6D3444),
        onPrimaryContainer = Color(0xFFFFDDBA),
        secondary = Color(0xFFA98BF2),
        onSecondary = Color(0xFF1D1236),
        secondaryContainer = Color(0xFF4B355F),
        tertiary = Color(0xFFEF6A73),
        background = Color(0xFF17131A),
        onBackground = Color(0xFFF6EEF4),
        surface = Color(0xFF211B24),
        onSurface = Color(0xFFF6EEF4),
        surfaceVariant = Color(0xFF332A38),
        onSurfaceVariant = Color(0xFFCDBFD1),
        outline = Color(0xFF66536B),
        outlineVariant = Color(0xFF46394A)
    )
    MaterialTheme(
        colorScheme = scheme,
        shapes = androidx.compose.material3.Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}

private data class TreeEntry(
    val file: File,
    val depth: Int
)

private data class ApkSearchResult(
    val name: String,
    val path: String,
    val size: Long,
    val modified: Long,
    val file: File? = null,
    val uri: Uri? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ElegantExplorerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FileExplorer()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun FileExplorer() {
    val initialDirectory = Environment.getExternalStorageDirectory()
    val currentDirectory = remember { mutableStateOf(initialDirectory) }
    val gridMode = remember { mutableStateOf(false) }
    val searchQuery = remember { mutableStateOf("") }
    val category = remember { mutableStateOf(FileCategory.ALL) }
    val showTree = remember { mutableStateOf(false) }
    val showHidden = remember { mutableStateOf(false) }
    val expandedFolders = remember { mutableStateOf(setOf(initialDirectory.absolutePath)) }
    val detailsFile = remember { mutableStateOf<File?>(null) }
    val showMenu = remember { mutableStateOf(false) }
    val apkResults = remember { mutableStateOf<List<ApkSearchResult>>(emptyList()) }
    val showApkResults = remember { mutableStateOf(false) }
    val scanning = remember { mutableStateOf(false) }
    val selectedPaths = remember { mutableStateOf(setOf<String>()) }
    val clipboardPaths = remember { mutableStateOf(listOf<File>()) }
    val clipboardMove = remember { mutableStateOf(false) }
    val showDestinationPicker = remember { mutableStateOf(false) }
    val destinationIsMove = remember { mutableStateOf(false) }
    val showTransferMenu = remember { mutableStateOf(false) }
    val createFolderDialog = remember { mutableStateOf(false) }
    val newFolderName = remember { mutableStateOf("") }
    val renameDialog = remember { mutableStateOf<File?>(null) }
    val renameText = remember { mutableStateOf("") }
    val deleteDialog = remember { mutableStateOf(false) }
    val trashMode = remember { mutableStateOf(false) }
    val trashRefresh = remember { mutableStateOf(0) }
    val previewFile = remember { mutableStateOf<File?>(null) }
    val mediaFile = remember { mutableStateOf<File?>(null) }
    val archiveDialog = remember { mutableStateOf(false) }
    val archiveName = remember { mutableStateOf("archivo.zip") }
    val context = LocalContext.current
    val showHome = remember { mutableStateOf(false) }
    val favorites = remember { mutableStateOf(loadFavorites(context)) }
    val navigationHistory = remember { mutableStateOf(listOf(initialDirectory.absolutePath)) }
    val historyIndex = remember { mutableStateOf(0) }
    val openTabs = remember { mutableStateOf(listOf(initialDirectory.absolutePath)) }
    val sortMode = remember { mutableStateOf(SortMode.NAME) }
    val permissionGranted = remember { mutableStateOf(true) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        permissionGranted.value = grants.values.all { it }
    }
    val manualApkFolderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val found = mutableListOf<ApkSearchResult>()
            collectApksFromDocumentTree(DocumentFile.fromTreeUri(context, uri), found)
            apkResults.value = found.sortedByDescending { it.modified }
            showApkResults.value = true
        }
    }

    fun navigateTo(file: File) {
        if (!file.isDirectory || !file.exists()) return
        showHome.value = false
        if (currentDirectory.value.absolutePath != file.absolutePath) {
            currentDirectory.value = file
            if (!openTabs.value.contains(file.absolutePath)) {
                openTabs.value = openTabs.value + file.absolutePath
            }
            val trimmed = navigationHistory.value.take(historyIndex.value + 1)
            navigationHistory.value = trimmed + file.absolutePath
            historyIndex.value = navigationHistory.value.lastIndex
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            !Environment.isExternalStorageManager()
        ) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:com.example.lightexplorer")
            try {
                permissionGranted.value = false
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        } else if (!Environment.isExternalStorageManager()) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissionLauncher.launch(permissions)
        }
    }

    val filesState = produceState(
        initialValue = emptyList<FileItem>(),
        currentDirectory.value,
        searchQuery.value,
        category.value,
        showHidden.value,
        sortMode.value
    ) {
        value = emptyList()
        val directory = currentDirectory.value
        val query = searchQuery.value
        val includeHidden = showHidden.value
        val selectedCategory = category.value
        val selectedSort = sortMode.value
        value = withContext(Dispatchers.IO) {
            val results = if (query.isBlank()) {
                directory.listFiles()
                    ?.filter { includeHidden || !it.name.startsWith(".") }
                    ?.filter { it.isDirectory || matchesCategory(it, selectedCategory) }
                    .orEmpty()
            } else {
                searchFiles(initialDirectory, query, includeHidden, selectedCategory)
            }
            results
                .sortedWith(sortComparator(selectedSort))
                .map { it.toFileItem() }
        }
    }
    val files = filesState.value

    val treeEntries = remember(expandedFolders.value, showHidden.value) {
        buildTreeEntries(initialDirectory, expandedFolders.value, showHidden.value)
    }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.primary
            ),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showHome.value = true }) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }
                    if (currentDirectory.value != Environment.getExternalStorageDirectory()) {
                        IconButton(onClick = {
                            if (historyIndex.value > 0) {
                                historyIndex.value -= 1
                                val path = navigationHistory.value[historyIndex.value]
                                File(path).takeIf { it.isDirectory }?.let { currentDirectory.value = it }
                            } else {
                                currentDirectory.value.parentFile?.let { navigateTo(it) }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                    Text(
                        text = currentDirectory.value.name.ifEmpty { "Almacenamiento" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            actions = {
                IconButton(onClick = { showHidden.value = !showHidden.value }) {
                    Icon(
                        imageVector = if (showHidden.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showHidden.value) "Ocultar archivos ocultos" else "Mostrar archivos ocultos"
                    )
                }
                IconButton(onClick = { showTree.value = !showTree.value }) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = if (showTree.value) "Ocultar árbol" else "Mostrar árbol"
                    )
                }
                IconButton(onClick = { gridMode.value = !gridMode.value }) {
                    Icon(
                        imageVector = if (gridMode.value) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                        contentDescription = if (gridMode.value) "Vista de lista" else "Vista de cuadrícula"
                    )
                }
                Box {
                    IconButton(onClick = { showMenu.value = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(
                        expanded = showMenu.value,
                        onDismissRequest = { showMenu.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Buscar APKs automáticamente") },
                            onClick = {
                                showMenu.value = false
                                scanning.value = true
                                apkResults.value = searchApksAutomatically(initialDirectory)
                                scanning.value = false
                                showApkResults.value = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Buscar APKs en carpeta manual") },
                            onClick = {
                                showMenu.value = false
                                manualApkFolderLauncher.launch(null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Nueva carpeta") },
                            onClick = {
                                showMenu.value = false
                                newFolderName.value = ""
                                createFolderDialog.value = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (trashMode.value) "Volver al navegador" else "Papelera") },
                            onClick = {
                                trashMode.value = !trashMode.value
                                showHome.value = false
                                showMenu.value = false
                                selectedPaths.value = emptySet()
                            }
                        )
                        if (selectedPaths.value.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Comprimir selección") },
                                onClick = {
                                    archiveName.value = "archivo.zip"
                                    archiveDialog.value = true
                                    showMenu.value = false
                                }
                            )
                        }
                        SortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Ordenar: ${mode.label}" +
                                            if (sortMode.value == mode) "  ✓" else ""
                                    )
                                },
                                onClick = {
                                    sortMode.value = mode
                                    showMenu.value = false
                                }
                            )
                        }
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            openTabs.value.forEach { path ->
                val tabFolder = File(path)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentDirectory.value.absolutePath == path)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (currentDirectory.value.absolutePath == path)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { if (tabFolder.isDirectory) navigateTo(tabFolder) }
                            .padding(start = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tabFolder.name.ifEmpty { "Almacenamiento" },
                            modifier = Modifier.width(112.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = {
                            if (openTabs.value.size == 1) {
                                navigateTo(initialDirectory)
                            } else {
                                val remaining = openTabs.value - path
                                openTabs.value = remaining
                                if (currentDirectory.value.absolutePath == path) {
                                    remaining.lastOrNull()?.let { last ->
                                        File(last).takeIf { it.isDirectory }?.let { navigateTo(it) }
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar pestaña")
                        }
                    }
                }
            }
            IconButton(onClick = { navigateTo(initialDirectory) }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva pestaña")
            }
        }

        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Buscar en esta carpeta") },
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FileCategory.entries.forEach { item ->
                FilterChip(
                    selected = category.value == item,
                    onClick = { category.value = item },
                    label = { Text(item.label) }
                )
            }
        }

        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (showTree.value) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(220.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(treeEntries) { entry ->
                            FolderTreeItem(
                                entry = entry,
                                selected = currentDirectory.value.absolutePath == entry.file.absolutePath,
                                expanded = expandedFolders.value.contains(entry.file.absolutePath),
                                showHidden = showHidden.value,
                                onDetails = { detailsFile.value = entry.file },
                                onToggle = {
                                    val path = entry.file.absolutePath
                                    expandedFolders.value = if (expandedFolders.value.contains(path)) {
                                        expandedFolders.value - path
                                    } else {
                                        expandedFolders.value + path
                                    }
                                },
                                onOpen = { navigateTo(entry.file) }
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                if (trashMode.value) {
                    val trashFiles = remember(trashMode.value, trashRefresh.value) {
                        getTrashFiles(initialDirectory)
                    }
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Papelera",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (trashFiles.isNotEmpty()) {
                                Button(onClick = {
                                    trashFiles.forEach { deleteRecursively(it) }
                                    trashRefresh.value++
                                }) {
                                    Text("Vaciar")
                                }
                            }
                        }
                        if (trashFiles.isEmpty()) {
                            Text(
                                "La papelera está vacía",
                                modifier = Modifier.padding(top = 16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(trashFiles) { item ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp)
                                            .combinedClickable(
                                                onClick = {},
                                                onLongClick = { detailsFile.value = item }
                                            ),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(
                                                    text = formatSize(item.length()),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(onClick = {
                                                restoreFromTrash(item)
                                                trashRefresh.value++
                                            }) {
                                                Icon(Icons.Default.Add, contentDescription = "Restaurar")
                                            }
                                            IconButton(onClick = {
                                                deleteRecursively(item)
                                                trashRefresh.value++
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar definitivamente")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (showHome.value) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            val stat = remember(initialDirectory.absolutePath) {
                                val fs = StatFs(initialDirectory.absolutePath)
                                val available = fs.availableBlocksLong * fs.blockSizeLong
                                val total = fs.blockCountLong * fs.blockSizeLong
                                val used = total - available
                                Triple(total, used, available)
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Almacenamiento", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${formatSize(stat.second)} usados  •  ${formatSize(stat.third)} libres",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Total: ${formatSize(stat.first)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        item {
                            Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium)
                        }
                        val quickLocations = listOf(
                            Environment.getExternalStorageDirectory(),
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                        ).filterNotNull().distinctBy { it.absolutePath }
                        items(quickLocations) { location ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { navigateTo(location) },
                                        onLongClick = {
                                            favorites.value = toggleFavorite(favorites.value, location.absolutePath)
                                            saveFavorites(context, favorites.value)
                                        }
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary)
                                    Text(location.name.ifEmpty { "Almacenamiento" }, modifier = Modifier.weight(1f))
                                    if (favorites.value.contains(location.absolutePath)) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC857))
                                    }
                                }
                            }
                        }
                        item {
                            Text("Favoritos", style = MaterialTheme.typography.titleMedium)
                        }
                        val favoriteFolders = favorites.value.mapNotNull { path ->
                            File(path).takeIf { it.isDirectory && it.exists() }
                        }
                        if (favoriteFolders.isEmpty()) {
                            item { Text("Mantén pulsado un acceso rápido para añadirlo.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        } else {
                            items(favoriteFolders) { folder ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable { navigateTo(folder) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC857))
                                        Text(folder.absolutePath, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        IconButton(onClick = {
                                            favorites.value = favorites.value - folder.absolutePath
                                            saveFavorites(context, favorites.value)
                                        }) {
                                            Icon(Icons.Default.Close, null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (!permissionGranted.value) {
                    Text(
                        text = "Concede permiso de almacenamiento para ver los archivos.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (files.isEmpty()) {
                    Text(
                        text = "Esta carpeta está vacía",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (gridMode.value) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(files) { item ->
                            FileGridItem(
                                item = item,
                                selected = selectedPaths.value.contains(item.file.absolutePath),
                                onClick = {
                                    if (item.isDirectory) navigateTo(item.file)
                                    else if (isMediaFile(item.file)) mediaFile.value = item.file
                                    else if (isImageFile(item.file)) previewFile.value = item.file
                                    else openFile(context, item.file)
                                },
                                onDetails = { detailsFile.value = item.file },
                                onSelect = {
                                    val path = item.file.absolutePath
                                    selectedPaths.value = if (selectedPaths.value.contains(path))
                                        selectedPaths.value - path else selectedPaths.value + path
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(files) { item ->
                            FileListItem(
                                item = item,
                                selected = selectedPaths.value.contains(item.file.absolutePath),
                                onClick = {
                                    if (item.isDirectory) navigateTo(item.file)
                                    else if (isMediaFile(item.file)) mediaFile.value = item.file
                                    else if (isImageFile(item.file)) previewFile.value = item.file
                                    else openFile(context, item.file)
                                },
                                onDetails = { detailsFile.value = item.file },
                                onSelect = {
                                    val path = item.file.absolutePath
                                    selectedPaths.value = if (selectedPaths.value.contains(path))
                                        selectedPaths.value - path else selectedPaths.value + path
                                }
                            )
                        }
                    }
                }
            }
        }
        if (selectedPaths.value.isNotEmpty() || clipboardPaths.value.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { selectedPaths.value = emptySet() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar selección")
                    }
                    Text(
                        if (selectedPaths.value.isNotEmpty()) {
                            "${selectedPaths.value.size} seleccionados" +
                                if (clipboardPaths.value.isNotEmpty()) {
                                    " · ${if (clipboardMove.value) "cortados" else "copiados"}: ${clipboardPaths.value.size}"
                                } else ""
                        }
                        else "${clipboardPaths.value.size} listos para ${if (clipboardMove.value) "mover" else "copiar"}"
                    )
                    if (selectedPaths.value.isNotEmpty()) {
                        IconButton(onClick = {
                            clipboardPaths.value = selectedPaths.value.mapNotNull { path ->
                                File(path).takeIf { it.exists() }
                            }
                            clipboardMove.value = false
                        }) { Icon(Icons.Default.ContentCopy, contentDescription = "Copiar") }
                        IconButton(onClick = {
                            clipboardPaths.value = selectedPaths.value.mapNotNull { path ->
                                File(path).takeIf { it.exists() }
                            }
                            clipboardMove.value = true
                        }) { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = "Cortar") }
                        Box {
                            IconButton(onClick = { showTransferMenu.value = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Copiar o mover a otra carpeta")
                            }
                            DropdownMenu(
                                expanded = showTransferMenu.value,
                                onDismissRequest = { showTransferMenu.value = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Copiar a…") },
                                    onClick = {
                                        clipboardPaths.value = selectedPaths.value.mapNotNull { path ->
                                            File(path).takeIf { it.exists() }
                                        }
                                        clipboardMove.value = false
                                        destinationIsMove.value = false
                                        showTransferMenu.value = false
                                        showDestinationPicker.value = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Mover a…") },
                                    onClick = {
                                        clipboardPaths.value = selectedPaths.value.mapNotNull { path ->
                                            File(path).takeIf { it.exists() }
                                        }
                                        clipboardMove.value = true
                                        destinationIsMove.value = true
                                        showTransferMenu.value = false
                                        showDestinationPicker.value = true
                                    }
                                )
                            }
                        }
                        IconButton(onClick = {
                            val targets = selectedPaths.value.mapNotNull { path ->
                                File(path).takeIf { it.exists() }
                            }
                            if (targets.size == 1) {
                                renameText.value = targets.first().name
                                renameDialog.value = targets.first()
                            }
                        }) { Icon(Icons.Default.Edit, contentDescription = "Renombrar") }
                        IconButton(onClick = {
                            shareFiles(context, selectedPaths.value.mapNotNull { path ->
                                File(path).takeIf { it.exists() }
                            })
                        }) { Icon(Icons.Default.Share, contentDescription = "Compartir") }
                        IconButton(onClick = { deleteDialog.value = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                        IconButton(onClick = {
                            archiveName.value = "archivo.zip"
                            archiveDialog.value = true
                        }) {
                            Icon(Icons.Default.Archive, contentDescription = "Comprimir selección")
                        }
                        if (selectedPaths.value.size == 1) {
                            val selectedZip = selectedPaths.value.firstOrNull()?.let { File(it) }
                            if (selectedZip?.extension?.lowercase(Locale.US) == "zip") {
                                IconButton(onClick = {
                                    extractZip(selectedZip, File(selectedZip.parentFile, selectedZip.nameWithoutExtension))
                                    selectedPaths.value = emptySet()
                                }) {
                                    Icon(Icons.Default.Archive, contentDescription = "Extraer ZIP")
                                }
                            }
                        }
                    }
                    if (clipboardPaths.value.isNotEmpty()) {
                        IconButton(onClick = {
                            val result = transferFiles(
                                clipboardPaths.value,
                                currentDirectory.value,
                                clipboardMove.value
                            )
                            if (clipboardMove.value) {
                                clipboardPaths.value = clipboardPaths.value.filter { it.exists() }
                            }
                            selectedPaths.value = emptySet()
                            Toast.makeText(
                                context,
                                "Pegados: ${result.succeeded}; fallidos: ${result.failed}",
                                Toast.LENGTH_LONG
                            ).show()
                        }) { Icon(Icons.Default.Add, contentDescription = "Pegar aquí") }
                    }
                }
            }
        }
        if (showDestinationPicker.value) {
            FolderDestinationDialog(
                startDirectory = currentDirectory.value,
                boundaryDirectory = initialDirectory,
                moving = destinationIsMove.value,
                onDismiss = { showDestinationPicker.value = false },
                onChoose = { destination ->
                    val result = transferFiles(
                        clipboardPaths.value,
                        destination,
                        destinationIsMove.value
                    )
                    if (destinationIsMove.value) {
                        clipboardPaths.value = clipboardPaths.value.filter { it.exists() }
                    }
                    selectedPaths.value = emptySet()
                    showDestinationPicker.value = false
                    Toast.makeText(
                        context,
                        "${if (destinationIsMove.value) "Movidos" else "Copiados"}: ${result.succeeded}; fallidos: ${result.failed}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
        if (archiveDialog.value) {
            AlertDialog(
                onDismissRequest = { archiveDialog.value = false },
                title = { Text("Comprimir archivos") },
                text = {
                    OutlinedTextField(
                        value = archiveName.value,
                        onValueChange = { archiveName.value = it },
                        singleLine = true,
                        label = { Text("Nombre del ZIP") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val name = if (archiveName.value.trim().endsWith(".zip", ignoreCase = true))
                            archiveName.value.trim()
                        else "${archiveName.value.trim().ifEmpty { "archivo" }}.zip"
                        val sources = selectedPaths.value.mapNotNull { path ->
                            File(path).takeIf { it.exists() }
                        }
                        val target = uniqueTarget(currentDirectory.value, name)
                        createZip(target, sources)
                        selectedPaths.value = emptySet()
                        archiveDialog.value = false
                    }) { Text("Comprimir") }
                },
                dismissButton = {
                    Button(onClick = { archiveDialog.value = false }) { Text("Cancelar") }
                }
            )
        }
        if (createFolderDialog.value) {
            AlertDialog(
                onDismissRequest = { createFolderDialog.value = false },
                title = { Text("Nueva carpeta") },
                text = {
                    OutlinedTextField(
                        value = newFolderName.value,
                        onValueChange = { newFolderName.value = it },
                        singleLine = true,
                        label = { Text("Nombre") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val name = newFolderName.value.trim()
                        if (name.isNotEmpty()) uniqueTarget(currentDirectory.value, name).mkdir()
                        createFolderDialog.value = false
                    }) { Text("Crear") }
                },
                dismissButton = {
                    Button(onClick = { createFolderDialog.value = false }) { Text("Cancelar") }
                }
            )
        }
        renameDialog.value?.let { source ->
            AlertDialog(
                onDismissRequest = { renameDialog.value = null },
                title = { Text("Renombrar") },
                text = {
                    OutlinedTextField(
                        value = renameText.value,
                        onValueChange = { renameText.value = it },
                        singleLine = true,
                        label = { Text("Nuevo nombre") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val name = renameText.value.trim()
                        if (name.isNotEmpty()) {
                            val target = File(source.parentFile, name)
                            if (!target.exists()) source.renameTo(target)
                        }
                        renameDialog.value = null
                        selectedPaths.value = emptySet()
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    Button(onClick = { renameDialog.value = null }) { Text("Cancelar") }
                }
            )
        }
        if (deleteDialog.value) {
            AlertDialog(
                onDismissRequest = { deleteDialog.value = false },
                title = { Text("Eliminar selección") },
                text = { Text("¿Eliminar ${selectedPaths.value.size} elemento(s)? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(onClick = {
                        selectedPaths.value.forEach { path ->
                            moveToTrash(initialDirectory, File(path))
                        }
                        selectedPaths.value = emptySet()
                        deleteDialog.value = false
                    }) { Text("Eliminar") }
                },
                dismissButton = {
                    Button(onClick = { deleteDialog.value = false }) { Text("Cancelar") }
                }
            )
        }
        detailsFile.value?.let { file ->
            AlertDialog(
                onDismissRequest = { detailsFile.value = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(file.name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (file.isDirectory) {
                            IconButton(onClick = {
                                favorites.value = toggleFavorite(favorites.value, file.absolutePath)
                                saveFavorites(context, favorites.value)
                            }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Alternar favorito",
                                    tint = if (favorites.value.contains(file.absolutePath))
                                        Color(0xFFFFC857) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                text = {
                    LazyColumn {
                        items(buildFileDetails(file)) { line ->
                            Text(text = line, modifier = Modifier.padding(vertical = 3.dp))
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (file.isDirectory) navigateTo(file)
                        else if (isMediaFile(file)) mediaFile.value = file
                        else openFile(context, file)
                        detailsFile.value = null
                    }) {
                        Text(if (file.isDirectory) "Abrir carpeta" else "Elegir / abrir app")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!file.isDirectory && isMediaFile(file)) {
                            Button(onClick = {
                                mediaFile.value = file
                                detailsFile.value = null
                            }) { Text("Reproducir") }
                        }
                        if (!file.isDirectory && canPreview(file)) {
                            Button(onClick = {
                                previewFile.value = file
                                detailsFile.value = null
                            }) { Text("Vista previa") }
                        }
                        Button(onClick = { detailsFile.value = null }) { Text("Cerrar") }
                    }
                }
            )
        }
        previewFile.value?.let { file ->
            AlertDialog(
                onDismissRequest = { previewFile.value = null },
                title = { Text(file.name) },
                text = {
                    val mime = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(file.extension.lowercase(Locale.US))
                        .orEmpty()
                    if (mime.startsWith("image/")
                        || file.extension.lowercase(Locale.US) in setOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "avif", "heic", "heif")
                    ) {
                        ImagePreview(file = file)
                    } else if (mime.startsWith("text/") || file.extension.lowercase(Locale.US) in setOf("txt", "csv", "json", "xml", "html", "md", "log", "kt", "java", "py", "js", "ts")) {
                        val preview = remember(file.absolutePath) { readTextPreview(file) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(preview, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        openFile(context, file)
                        previewFile.value = null
                    }) { Text("Abrir") }
                },
                dismissButton = {
                    Button(onClick = { previewFile.value = null }) { Text("Cerrar") }
                }
            )
        }
        mediaFile.value?.let { file ->
            NativeMediaDialog(
                file = file,
                onDismiss = { mediaFile.value = null }
            )
        }
        if (showApkResults.value) {
            AlertDialog(
                onDismissRequest = { showApkResults.value = false },
                title = { Text("APK encontrados (${apkResults.value.size})") },
                text = {
                    if (apkResults.value.isEmpty()) {
                        Text("No se encontraron paquetes APK.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(apkResults.value) { result ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .combinedClickable(
                                            onClick = {
                                                result.file?.let { openFile(context, it) }
                                                    ?: result.uri?.let { openUri(context, it) }
                                                showApkResults.value = false
                                            },
                                            onLongClick = {
                                                result.file?.let { detailsFile.value = it }
                                            }
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(result.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            text = "${formatSize(result.size)}  •  ${result.path}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showApkResults.value = false }) { Text("Cerrar") }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderTreeItem(
    entry: TreeEntry,
    selected: Boolean,
    expanded: Boolean,
    showHidden: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    onDetails: () -> Unit
) {
    val scale = animateFloatAsState(
        targetValue = if (selected) 1.025f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "folderScale"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .combinedClickable(onClick = onOpen, onLongClick = onDetails)
            .padding(start = (8 + entry.depth * 12).dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val hasChildren = entry.file.listFiles()?.any {
            it.isDirectory && (showHidden || !it.name.startsWith("."))
        } == true
        if (hasChildren) {
            IconButton(onClick = onToggle, modifier = Modifier.padding(0.dp)) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (expanded) "Contraer" else "Expandir"
                )
            }
        } else {
            Box(modifier = Modifier.width(48.dp))
        }
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = entry.file.name.ifEmpty { "Almacenamiento" },
            modifier = Modifier.weight(1f).padding(start = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDetails) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Información de la carpeta",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun buildTreeEntries(
    root: File,
    expandedPaths: Set<String>,
    showHidden: Boolean
): List<TreeEntry> {
    val result = mutableListOf<TreeEntry>()

    fun addFolder(folder: File, depth: Int) {
        result.add(TreeEntry(folder, depth))
        if (!expandedPaths.contains(folder.absolutePath)) return

        val children = try {
            folder.listFiles()
                ?.filter { it.isDirectory && (showHidden || !it.name.startsWith(".")) }
                ?.sortedBy { it.name.lowercase(Locale.US) }
                .orEmpty()
        } catch (_: SecurityException) {
            emptyList()
        }
        children.forEach { addFolder(it, depth + 1) }
    }

    addFolder(root, 0)
    return result
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderDestinationDialog(
    startDirectory: File,
    boundaryDirectory: File,
    moving: Boolean,
    onDismiss: () -> Unit,
    onChoose: (File) -> Unit
) {
    val destination = remember(startDirectory.absolutePath) {
        mutableStateOf(startDirectory)
    }
    val folders = produceState(
        initialValue = emptyList<File>(),
        destination.value.absolutePath
    ) {
        val directory = destination.value
        value = withContext(Dispatchers.IO) {
            directory.listFiles()
                ?.filter { it.isDirectory && !it.name.startsWith(".") }
                ?.sortedBy { it.name.lowercase(Locale.US) }
                .orEmpty()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (moving) "Mover a…" else "Copiar a…") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val parent = destination.value.parentFile
                            if (parent != null && parent.canonicalFile.path.startsWith(boundaryDirectory.canonicalPath)) {
                                destination.value = parent
                            }
                        },
                        enabled = destination.value.canonicalFile != boundaryDirectory.canonicalFile &&
                            destination.value.parentFile?.canonicalFile?.path?.startsWith(boundaryDirectory.canonicalPath) == true
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Carpeta anterior")
                    }
                    Text(
                        destination.value.absolutePath,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    items(folders.value, key = { it.absolutePath }) { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { destination.value = folder }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null)
                            Text(folder.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onChoose(destination.value) },
                enabled = destination.value.isDirectory && destination.value.canWrite()
            ) {
                Text(if (moving) "Mover aquí" else "Copiar aquí")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
    item: FileItem,
    selected: Boolean,
    onClick: () -> Unit,
    onDetails: () -> Unit,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onSelect)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = if (item.isDirectory) Color(0xFFECA45B) else Color(0xFFA98BF2)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = if (item.isDirectory) item.modified else "${item.size}  •  ${item.modified}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isMediaFile(item.file)) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reproducir ${item.name}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDetails) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = if (item.isDirectory) "Información de la carpeta" else "Información del archivo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileGridItem(
    item: FileItem,
    selected: Boolean,
    onClick: () -> Unit,
    onDetails: () -> Unit,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .combinedClickable(onClick = onClick, onLongClick = onSelect),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDetails) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = if (item.isDirectory) "Información de la carpeta" else "Información del archivo"
                    )
                }
            }
            Icon(
                imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(item.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun File.toFileItem(): FileItem {
    val date = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(lastModified()))
    val size = if (isDirectory) "" else formatSize(length())
    return FileItem(this, name, isDirectory, size, date)
}

private fun openFile(context: android.content.Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val extension = file.extension.lowercase(Locale.US)
    val mime = if (extension == "apk") {
        "application/vnd.android.package-archive"
    } else {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Abrir con"))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/octet-stream")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        )
    }
}

private fun openUri(context: android.content.Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Abrir APK con"))
}

private fun buildFileDetails(file: File): List<String> {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val type = if (file.isDirectory) "Carpeta" else when (file.extension.lowercase(Locale.US)) {
        "apk" -> "Paquete Android (.apk)"
        "zip" -> "Archivo comprimido ZIP"
        "rar" -> "Archivo comprimido RAR"
        "pdf" -> "Documento PDF"
        else -> MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.extension.lowercase(Locale.US))
            ?: "Archivo"
    }
    val directCount = try {
        file.listFiles()?.size
    } catch (_: SecurityException) {
        null
    }
    val measuredSize = if (file.isDirectory) folderSize(file) else file.length()
    return listOf(
        "Tipo: $type",
        "Ruta: ${file.absolutePath}",
        "Tamaño: ${formatSize(measuredSize)}",
        "Modificado: ${dateFormat.format(Date(file.lastModified()))}",
        "Elementos inmediatos: ${directCount?.toString() ?: "No disponible"}",
        "Lectura: ${if (file.canRead()) "Sí" else "No"}",
        "Escritura: ${if (file.canWrite()) "Sí" else "No"}",
        "Oculto: ${if (file.name.startsWith(".")) "Sí" else "No"}"
    )
}

private fun folderSize(root: File): Long {
    var total = 0L
    var visited = 0
    fun walk(file: File) {
        if (visited >= 5000) return
        visited++
        if (file.isDirectory) {
            try {
                file.listFiles()?.forEach { walk(it) }
            } catch (_: SecurityException) {
            }
        } else {
            total += file.length()
        }
    }
    walk(root)
    return total
}

private fun searchApksAutomatically(root: File): List<ApkSearchResult> {
    val roots = mutableSetOf(root)
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.let { roots += it }
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)?.let { roots += it }
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)?.let { roots += it }
    val found = mutableListOf<ApkSearchResult>()
    var visited = 0
    fun walk(folder: File) {
        if (visited >= 30000) return
        val children = try {
            folder.listFiles()
        } catch (_: SecurityException) {
            null
        } ?: return
        children.forEach { child ->
            if (visited++ >= 30000) return
            if (child.isDirectory) {
                if (!child.name.startsWith(".")) walk(child)
            } else if (child.extension.equals("apk", ignoreCase = true)) {
                found += ApkSearchResult(
                    name = child.name,
                    path = child.absolutePath,
                    size = child.length(),
                    modified = child.lastModified(),
                    file = child
                )
            }
        }
    }
    roots.filter { it.exists() }.forEach { walk(it) }
    return found
        .distinctBy { it.path }
        .sortedByDescending { it.modified }
}

private fun collectApksFromDocumentTree(
    document: DocumentFile?,
    found: MutableList<ApkSearchResult>,
    visited: IntArray = IntArray(1)
) {
    if (document == null || visited[0] >= 30000) return
    document.listFiles().forEach { child ->
        if (visited[0]++ >= 30000) return
        if (child.isDirectory) {
            collectApksFromDocumentTree(child, found, visited)
        } else if (child.name?.endsWith(".apk", ignoreCase = true) == true) {
            found += ApkSearchResult(
                name = child.name ?: "archivo.apk",
                path = child.uri.path ?: child.uri.toString(),
                size = child.length(),
                modified = child.lastModified(),
                uri = child.uri
            )
        }
    }
}

private fun searchFiles(
        root: File,
        query: String,
        showHidden: Boolean,
        category: FileCategory
    ): List<File> {
        val found = mutableListOf<File>()
        var visited = 0
        fun walk(folder: File) {
            if (visited >= 30000) return
            val children = try {
                folder.listFiles()
            } catch (_: SecurityException) {
                null
            } ?: return
            children.forEach { child ->
                if (visited++ >= 30000) return
                if (!showHidden && child.name.startsWith(".")) return@forEach
                if (child.name.contains(query, ignoreCase = true)
                    && (child.isDirectory || matchesCategory(child, category))
                ) {
                    found += child
                }
                if (child.isDirectory) walk(child)
            }
        }
        walk(root)
        return found.sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase(Locale.US) })
    }

private fun uniqueTarget(parent: File, name: String): File {
        var target = File(parent, name)
        var index = 1
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val extension = if (dot > 0) name.substring(dot) else ""
        while (target.exists()) {
            target = File(parent, "$base ($index)$extension")
            index++
        }
        return target
    }

private data class TransferResult(val succeeded: Int, val failed: Int)

private fun transferFiles(sources: List<File>, destination: File, move: Boolean): TransferResult {
    if (!destination.isDirectory || !destination.canWrite()) {
        return TransferResult(0, sources.size)
    }

    var succeeded = 0
    var failed = 0
    sources.forEach { source ->
        val success = try {
            val sourcePath = source.canonicalFile
            val destinationPath = destination.canonicalFile
            val sameDirectory = sourcePath.parentFile == destinationPath
            val copyingIntoSelf = source.isDirectory &&
                destinationPath.path.startsWith(sourcePath.path + File.separator)
            if (!source.exists() || sourcePath == destinationPath || (move && sameDirectory) || copyingIntoSelf) {
                false
            } else {
                val target = uniqueTarget(destination, source.name)
                if (move) moveFile(source, target) else copyFile(source, target)
            }
        } catch (_: Exception) {
            false
        }
        if (success) succeeded++ else failed++
    }
    return TransferResult(succeeded, failed)
}

private fun copyFile(source: File, target: File): Boolean {
        if (source.canonicalFile == target.canonicalFile || target.exists()) return false
        return try {
            if (source.isDirectory) {
                if (!target.mkdirs() && !target.isDirectory) return false
                val children = source.listFiles() ?: return false
                val copied = children.all { child -> copyFile(child, File(target, child.name)) }
                if (!copied) {
                    deleteRecursively(target)
                } else {
                    target.setLastModified(source.lastModified())
                }
                copied
            } else {
                target.parentFile?.let { parent ->
                    if (!parent.exists() && !parent.mkdirs()) return false
                }
                source.inputStream().use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                target.setLastModified(source.lastModified())
                true
            }
        } catch (_: Exception) {
            if (target.exists()) deleteRecursively(target)
            false
        }
    }

private fun moveFile(source: File, target: File): Boolean {
        if (source.canonicalFile == target.canonicalFile || target.exists()) return false
        return try {
            if (source.renameTo(target)) true else {
                if (copyFile(source, target)) {
                    if (deleteRecursively(source)) true else {
                        deleteRecursively(target)
                        false
                    }
                } else false
            }
        } catch (_: Exception) {
            false
        }
    }

private fun deleteRecursively(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { deleteRecursively(it) }
            }
            file.delete()
        } catch (_: Exception) {
            false
        }
    }

private fun shareFiles(context: android.content.Context, files: List<File>) {
        if (files.isEmpty()) return
        val uris = files.map { file ->
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uris.first())
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Compartir"))
    }

private fun loadFavorites(context: android.content.Context): Set<String> {
    return context.getSharedPreferences("file_digger", android.content.Context.MODE_PRIVATE)
        .getStringSet("favorites", emptySet())
        ?.toSet()
        ?: emptySet()
}

private fun saveFavorites(context: android.content.Context, favorites: Set<String>) {
    context.getSharedPreferences("file_digger", android.content.Context.MODE_PRIVATE)
        .edit()
        .putStringSet("favorites", favorites)
        .apply()
}

private fun toggleFavorite(favorites: Set<String>, path: String): Set<String> {
    return if (favorites.contains(path)) favorites - path else favorites + path
}

private fun getTrashFolder(root: File): File {
    val folder = File(root, ".FileDiggerTrash")
    if (!folder.exists()) folder.mkdirs()
    return folder
}

private fun getTrashFiles(root: File): List<File> {
    return getTrashFolder(root)
        .listFiles()
        ?.filter { (it.isFile || it.isDirectory) && !it.name.endsWith(".origin") }
        ?.sortedByDescending { it.lastModified() }
        .orEmpty()
}

private fun moveToTrash(root: File, source: File): Boolean {
    if (!source.exists()) return false
    val trash = getTrashFolder(root)
    val timestamp = System.currentTimeMillis()
    val target = uniqueTarget(trash, "${timestamp}_${source.name}")
    val origin = File(trash, "${target.name}.origin")
    return try {
        origin.writeText(source.absolutePath)
        if (source.renameTo(target)) true else {
            if (copyFile(source, target)) {
                deleteRecursively(source)
                true
            } else {
                origin.delete()
                false
            }
        }
    } catch (_: Exception) {
        false
    }
}

private fun restoreFromTrash(trashFile: File): Boolean {
    val origin = File(trashFile.parentFile, "${trashFile.name}.origin")
    val originalPath = try {
        if (origin.exists()) origin.readText().trim() else ""
    } catch (_: Exception) {
        ""
    }
    val target = if (originalPath.isNotEmpty()) {
        val original = File(originalPath)
        val parent = original.parentFile
        if (parent != null && parent.exists()) uniqueTarget(parent, original.name)
        else uniqueTarget(trashFile.parentFile?.parentFile ?: trashFile.parentFile, original.name)
    } else {
        uniqueTarget(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                ?: trashFile.parentFile?.parentFile ?: trashFile.parentFile,
            trashFile.name.substringAfter("_")
        )
    }
    return try {
        if (trashFile.renameTo(target)) {
            origin.delete()
            true
        } else {
            false
        }
    } catch (_: Exception) {
        false
    }
}

private fun matchesCategory(file: File, category: FileCategory): Boolean {
    val extension = file.extension.lowercase(Locale.US)
    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).orEmpty()
    return when (category) {
        FileCategory.ALL -> true
        FileCategory.APKS -> extension == "apk"
        FileCategory.IMAGES -> mime.startsWith("image/")
        FileCategory.VIDEOS -> mime.startsWith("video/")
        FileCategory.AUDIO -> mime.startsWith("audio/")
        FileCategory.DOCUMENTS -> mime.startsWith("text/")
            || mime in setOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            )
        FileCategory.ARCHIVES -> extension in setOf(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz"
        )
        FileCategory.OTHERS -> !mime.startsWith("image/")
            && !mime.startsWith("video/")
            && !mime.startsWith("audio/")
            && !mime.startsWith("text/")
            && extension != "apk"
            && extension !in setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx")
            && extension !in setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")
    }
}

private fun canPreview(file: File): Boolean {
    val extension = file.extension.lowercase(Locale.US)
    val mime = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(extension)
        .orEmpty()
    return mime.startsWith("image/")
        || extension in setOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "avif", "heic", "heif")
        || mime.startsWith("text/")
        || extension in setOf("txt", "csv", "json", "xml", "html", "md", "log", "kt", "java", "py", "js", "ts")
}

private fun decodeNativeBitmap(file: File): android.graphics.Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
                val largest = maxOf(info.size.width, info.size.height)
                val sample = (largest / 1600).coerceAtLeast(1)
                decoder.setTargetSampleSize(sample)
            }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        file.inputStream().use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        var sample = 1
        while (bounds.outWidth / sample > 1600 || bounds.outHeight / sample > 1600) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        file.inputStream().use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    } catch (_: Exception) {
        null
    }
}

private fun decodeNativeDrawable(file: File): Drawable? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
    return try {
        ImageDecoder.decodeDrawable(ImageDecoder.createSource(file))
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun ImagePreview(file: File) {
    val scale = remember { mutableStateOf(1f) }
    val drawable = remember(file.absolutePath) { decodeNativeDrawable(file) }
    val animated = drawable as? AnimatedImageDrawable
    if (animated != null) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale.value = (scale.value * zoom).coerceIn(0.5f, 4f)
                        }
                    },
                factory = { context ->
                    android.widget.ImageView(context).apply {
                        scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                        setImageDrawable(animated)
                        animated.start()
                    }
                },
                onRelease = { animated.stop() }
            )
            ImageZoomControls(scale)
        }
    } else {
        val bitmap = remember(file.absolutePath) {
            decodeNativeBitmap(file)
        }
        if (bitmap != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Imagen ${file.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                scale.value = (scale.value * zoom).coerceIn(0.5f, 4f)
                            }
                        },
                    contentScale = ContentScale.Fit
                )
                ImageZoomControls(scale)
            }
        } else {
            Text("No se pudo cargar la imagen.")
        }
    }
}

@Composable
private fun ImageZoomControls(scale: androidx.compose.runtime.MutableState<Float>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { scale.value = (scale.value - 0.25f).coerceAtLeast(0.5f) }) {
            Text("-", style = MaterialTheme.typography.titleLarge)
        }
        Text("${(scale.value * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        IconButton(onClick = { scale.value = (scale.value + 0.25f).coerceAtMost(4f) }) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
        IconButton(onClick = { scale.value = 1f }) {
            Text("1:1", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun isImageFile(file: File): Boolean {
    val extension = file.extension.lowercase(Locale.US)
    val mime = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(extension)
        .orEmpty()
    return mime.startsWith("image/")
        || extension in setOf(
            "png", "jpg", "jpeg", "gif", "webp", "bmp", "avif", "heic", "heif",
            "tif", "tiff", "svg", "ico", "jfif", "dng", "raw", "apng"
        )
}

private fun isMediaFile(file: File): Boolean {
    val mime = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(file.extension.lowercase(Locale.US))
        .orEmpty()
    return mime.startsWith("audio/")
        || mime.startsWith("video/")
        || file.extension.lowercase(Locale.US) in setOf(
            "mp3", "m4a", "aac", "wav", "ogg", "oga", "opus", "flac", "amr", "awb",
            "wma", "aiff", "aif", "mid", "midi", "mka", "mp2", "ac3",
            "mp4", "mkv", "webm", "avi", "mov", "3gp", "3g2", "m4v", "mts", "m2ts",
            "ts", "wmv", "flv", "ogv", "mpeg", "mpg", "vob", "rmvb", "asf"
        )
}

private fun isVideoFile(file: File): Boolean {
    val mime = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(file.extension.lowercase(Locale.US))
        .orEmpty()
    return mime.startsWith("video/")
        || file.extension.lowercase(Locale.US) in setOf(
            "mp4", "mkv", "webm", "avi", "mov", "3gp", "3g2", "m4v", "mts", "m2ts",
            "ts", "wmv", "flv", "ogv", "mpeg", "mpg", "vob", "rmvb", "asf"
        )
}

@Composable
private fun NativeMediaDialog(file: File, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isVideo = isVideoFile(file)
    val position = remember { mutableStateOf(0) }
    val duration = remember { mutableStateOf(1) }
    val playing = remember { mutableStateOf(false) }
    val prepared = remember { mutableStateOf(false) }
    val configured = remember { mutableStateOf(false) }
    val videoSurface = remember { mutableStateOf<Surface?>(null) }
    val volume = remember { mutableStateOf(1f) }
    val windowScale = remember { mutableStateOf(1f) }
    val offsetX = remember { mutableStateOf(18f) }
    val offsetY = remember { mutableStateOf(120f) }
    val error = remember { mutableStateOf<String?>(null) }
    val player = remember(file.absolutePath) { MediaPlayer() }

    LaunchedEffect(file.absolutePath, videoSurface.value) {
        if (configured.value || (isVideo && videoSurface.value == null)) return@LaunchedEffect
        configured.value = true
        try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(if (isVideo) AudioAttributes.CONTENT_TYPE_MOVIE else AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            if (isVideo) player.setSurface(videoSurface.value)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            player.setDataSource(context, uri)
            player.setOnPreparedListener { media ->
                prepared.value = true
                duration.value = media.duration.coerceAtLeast(1)
                media.setVolume(volume.value, volume.value)
                media.start()
                playing.value = true
            }
            player.setOnCompletionListener {
                playing.value = false
                position.value = duration.value
            }
            player.setOnErrorListener { _, what, extra ->
                playing.value = false
                error.value = "Android no pudo decodificar este archivo ($what/$extra)"
                true
            }
            player.prepareAsync()
        } catch (failure: Exception) {
            error.value = failure.message ?: "No se pudo abrir el archivo multimedia"
        }
    }

    DisposableEffect(player) {
        val handler = Handler(Looper.getMainLooper())
        val updateProgress = object : Runnable {
            override fun run() {
                if (prepared.value) {
                    try {
                        position.value = player.currentPosition.coerceAtLeast(0)
                        duration.value = player.duration.coerceAtLeast(1)
                        playing.value = player.isPlaying
                    } catch (_: IllegalStateException) {
                        playing.value = false
                    }
                }
                handler.postDelayed(this, 250)
            }
        }
        handler.post(updateProgress)
        onDispose {
            handler.removeCallbacks(updateProgress)
            try {
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                player.reset()
                player.release()
            } catch (_: IllegalStateException) {
            }
            videoSurface.value?.release()
            videoSurface.value = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)
        ) {}
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    translationX = offsetX.value
                    translationY = offsetY.value
                    scaleX = windowScale.value
                    scaleY = windowScale.value
                }
                .width(360.dp)
                .height(if (isVideo) 520.dp else 420.dp)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        windowScale.value = (windowScale.value * zoom).coerceIn(0.65f, 2.2f)
                    }
                },
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 14.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectDragGestures { _, drag ->
                                offsetX.value = (offsetX.value + drag.x).coerceIn(-100f, 600f)
                                offsetY.value = (offsetY.value + drag.y).coerceIn(0f, 900f)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        file.name,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar reproductor")
                    }
                }
                Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isVideo) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .padding(4.dp),
                        factory = { viewContext ->
                            TextureView(viewContext).apply {
                                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                    override fun onSurfaceTextureAvailable(
                                        surfaceTexture: android.graphics.SurfaceTexture,
                                        width: Int,
                                        height: Int
                                    ) {
                                        videoSurface.value?.release()
                                        videoSurface.value = Surface(surfaceTexture)
                                    }

                                    override fun onSurfaceTextureSizeChanged(
                                        surfaceTexture: android.graphics.SurfaceTexture,
                                        width: Int,
                                        height: Int
                                    ) = Unit

                                    override fun onSurfaceTextureDestroyed(
                                        surfaceTexture: android.graphics.SurfaceTexture
                                    ): Boolean {
                                        return true
                                    }

                                    override fun onSurfaceTextureUpdated(
                                        surfaceTexture: android.graphics.SurfaceTexture
                                    ) = Unit
                                }
                            }
                        },
                        onRelease = { view -> view.surfaceTextureListener = null }
                    )
                } else {
                    Icon(
                        imageVector = if (playing.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(96.dp).height(96.dp)
                    )
                }

                Slider(
                    value = position.value.toFloat().coerceIn(0f, duration.value.toFloat()),
                    onValueChange = { position.value = it.toInt() },
                    onValueChangeFinished = {
                        if (prepared.value) player.seekTo(position.value)
                    },
                    valueRange = 0f..duration.value.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "${formatMediaTime(position.value.toInt())} / ${formatMediaTime(duration.value.toInt())}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (prepared.value) player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0))
                    }) { Text("-10") }
                    IconButton(onClick = {
                        if (prepared.value) {
                            if (player.isPlaying) {
                                player.pause()
                                playing.value = false
                            } else {
                                player.start()
                                playing.value = true
                            }
                        }
                    }) {
                        Icon(
                            if (playing.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing.value) "Pausar" else "Reproducir"
                        )
                    }
                    IconButton(onClick = {
                        if (prepared.value) player.seekTo((player.currentPosition + 10_000).coerceAtMost(player.duration))
                    }) { Text("+10") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (volume.value <= 0f) Icons.AutoMirrored.Filled.VolumeOff
                        else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Volumen"
                    )
                    Slider(
                        value = volume.value,
                        onValueChange = { value ->
                            volume.value = value
                            player.setVolume(value, value)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f)
                    )
                }

                error.value?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (!prepared.value && error.value == null) {
                    Text("Preparando reproducción…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            }
        }
    }
    }
}

@Composable
private fun LegacyNativeMediaDialog(file: File, onDismiss: () -> Unit) {
    val mime = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(file.extension.lowercase(Locale.US))
        .orEmpty()
    val context = LocalContext.current
    val isVideo = mime.startsWith("video/")
        || file.extension.lowercase(Locale.US) in setOf("mp4", "mkv", "webm", "avi", "mov", "3gp")

    val fullscreen = remember { mutableStateOf(false) }
    val minimized = remember { mutableStateOf(false) }
    val offsetX = remember { mutableStateOf(18f) }
    val offsetY = remember { mutableStateOf(120f) }
    val panelWidth = remember { mutableStateOf(352f) }
    val panelHeight = remember { mutableStateOf(if (isVideo) 310f else 220f) }
    val playing = remember { mutableStateOf(false) }
    val position = remember { mutableStateOf(0f) }
    val volume = remember { mutableStateOf(1f) }
    val duration = remember { mutableStateOf(1f) }
    val mediaError = remember { mutableStateOf<String?>(null) }
    val player = remember { mutableStateOf<MediaPlayer?>(null) }
    val videoView = remember { mutableStateOf<VideoView?>(null) }
    val audioManager = remember(context) {
        context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
    }
    val maxVolume = remember(audioManager) { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    LaunchedEffect(Unit) {
        volume.value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() /
            maxVolume.coerceAtLeast(1).toFloat()
    }
    LaunchedEffect(playing.value) {
        while (playing.value) {
            position.value = if (isVideo) {
                videoView.value?.currentPosition?.toFloat() ?: position.value
            } else {
                player.value?.currentPosition?.toFloat() ?: position.value
            }
            delay(250)
        }
    }
    DisposableEffect(file.absolutePath) {
        onDispose {
            if (isVideo) videoView.value?.stopPlayback()
            else {
                if (player.value?.isPlaying == true) player.value?.stop()
                player.value?.release()
            }
        }
    }

    val windowModifier = if (fullscreen.value) {
        Modifier.fillMaxSize().padding(8.dp)
    } else {
        Modifier
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
            }
            .width(if (minimized.value) 190.dp else panelWidth.value.dp)
            .height(if (minimized.value) 72.dp else panelHeight.value.dp)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = windowModifier,
            shape = RoundedCornerShape(if (minimized.value) 16.dp else 22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 12.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectDragGestures { _, drag ->
                                if (!fullscreen.value && !minimized.value) {
                                    offsetX.value = (offsetX.value + drag.x / 2f).coerceIn(0f, 320f)
                                    offsetY.value = (offsetY.value + drag.y / 2f).coerceIn(0f, 700f)
                                }
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        file.name,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { minimized.value = !minimized.value }) {
                        Icon(
                            if (minimized.value) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (minimized.value) "Restaurar" else "Minimizar"
                        )
                    }
                    IconButton(onClick = { fullscreen.value = !fullscreen.value }) {
                        Icon(
                            if (fullscreen.value) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (fullscreen.value) "Salir de pantalla completa" else "Pantalla completa"
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar reproductor")
                    }
                }

                if (!minimized.value) {
                    if (isVideo) {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            factory = { context ->
                                VideoView(context).apply {
                                    setMediaController(null)
                                    setVideoURI(
                                        FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                    )
                                    setOnPreparedListener { media ->
                                        duration.value = media.duration.coerceAtLeast(1).toFloat()
                                        start()
                                        playing.value = true
                                    }
                                    videoView.value = this
                                }
                            },
                            onRelease = { view -> view.stopPlayback() }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (playing.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (mediaError.value != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(72.dp).height(72.dp)
                                )
                                mediaError.value?.let { error ->
                                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Slider(
                            value = position.value.coerceIn(0f, duration.value),
                            onValueChange = { position.value = it },
                            onValueChangeFinished = {
                                if (isVideo) videoView.value?.seekTo(position.value.toInt())
                                else player.value?.seekTo(position.value.toInt())
                            },
                            valueRange = 0f..duration.value
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val target = (position.value - 10000f).coerceAtLeast(0f)
                                position.value = target
                                if (isVideo) videoView.value?.seekTo(target.toInt())
                                else player.value?.seekTo(target.toInt())
                            }) { Text("-10") }
                            IconButton(onClick = {
                                if (isVideo) {
                                    val view = videoView.value
                                    if (view?.isPlaying == true) view.pause() else view?.start()
                                    playing.value = view?.isPlaying == true
                                } else {
                                    val current = player.value
                                    if (current == null) {
                                        try {
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            val created = MediaPlayer().apply {
                                                setAudioAttributes(
                                                    android.media.AudioAttributes.Builder()
                                                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                                        .build()
                                                )
                                                setDataSource(context, uri)
                                                setOnErrorListener { _, what, extra ->
                                                    mediaError.value = "Error de reproducción ($what/$extra)"
                                                    true
                                                }
                                                prepare()
                                            }
                                            player.value = created
                                            duration.value = created.duration.coerceAtLeast(1).toFloat()
                                            created.start()
                                            playing.value = true
                                        } catch (error: Exception) {
                                            mediaError.value = error.message ?: "No se pudo abrir el MP3"
                                        }
                                    } else if (current.isPlaying) {
                                        current.pause()
                                        playing.value = false
                                    } else {
                                        current.start()
                                        playing.value = true
                                        duration.value = current.duration.coerceAtLeast(1).toFloat()
                                    }
                                }
                            }) {
                                Icon(
                                    if (playing.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playing.value) "Pausar" else "Reproducir"
                                )
                            }
                            IconButton(onClick = {
                                val target = (position.value + 10000f).coerceAtMost(duration.value)
                                position.value = target
                                if (isVideo) videoView.value?.seekTo(target.toInt())
                                else player.value?.seekTo(target.toInt())
                            }) { Text("+10") }
                            Text(
                                "${formatMediaTime(position.value.toInt())} / ${formatMediaTime(duration.value.toInt())}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Icon(
                                if (volume.value <= 0f) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Volumen"
                            )
                            Slider(
                                value = volume.value,
                                onValueChange = { value ->
                                    volume.value = value
                                    val streamVolume = (value * maxVolume).toInt()
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVolume, 0)
                                },
                                valueRange = 0f..1f,
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }

                    if (!fullscreen.value) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .width(28.dp)
                                .height(28.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures { _, drag ->
                                        panelWidth.value = (panelWidth.value + drag.x / 2f).coerceIn(220f, 720f)
                                        panelHeight.value = (panelHeight.value + drag.y / 2f).coerceIn(180f, 520f)
                                    }
                                }
                        ) {
                            Text("◢", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

private fun formatMediaTime(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remaining = seconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, remaining)
}

private fun readTextPreview(file: File): String {
    return try {
        val text = file.readText(Charsets.UTF_8)
        if (text.length <= 12000) text else text.substring(0, 12000) + "\n\n… vista truncada"
    } catch (_: Exception) {
        "No se pudo leer el contenido como texto."
    }
}

private fun sortComparator(mode: SortMode): Comparator<File> {
    val folderFirst = compareByDescending<File> { it.isDirectory }
    return folderFirst.thenComparator { left, right ->
        when (mode) {
            SortMode.NAME -> left.name.lowercase(Locale.US).compareTo(right.name.lowercase(Locale.US))
            SortMode.SIZE -> right.length().compareTo(left.length())
            SortMode.MODIFIED -> right.lastModified().compareTo(left.lastModified())
            SortMode.TYPE -> left.extension.lowercase(Locale.US).compareTo(right.extension.lowercase(Locale.US))
        }
    }
}

private fun createZip(target: File, sources: List<File>): Boolean {
    if (sources.isEmpty()) return false
    return try {
        ZipOutputStream(target.outputStream()).use { zip ->
            sources.forEach { source ->
                addToZip(zip, source, "")
            }
        }
        true
    } catch (_: Exception) {
        target.delete()
        false
    }
}

private fun addToZip(zip: ZipOutputStream, file: File, prefix: String) {
    val entryName = prefix + file.name
    if (file.isDirectory) {
        val folderEntry = if (entryName.endsWith("/")) entryName else "$entryName/"
        zip.putNextEntry(ZipEntry(folderEntry))
        zip.closeEntry()
        file.listFiles()?.forEach { child ->
            addToZip(zip, child, "$entryName/")
        }
    } else {
        file.inputStream().use { input ->
            zip.putNextEntry(ZipEntry(entryName))
            input.copyTo(zip)
            zip.closeEntry()
        }
    }
}

private fun extractZip(zipFile: File, destination: File): Boolean {
    if (!zipFile.isFile || zipFile.extension.lowercase(Locale.US) != "zip") return false
    val root = destination.canonicalFile
    return try {
        destination.mkdirs()
        ZipInputStream(zipFile.inputStream()).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                val target = File(destination, entry.name)
                val canonical = target.canonicalFile
                if (canonical.path != root.path && !canonical.path.startsWith(root.path + File.separator)) {
                    zip.closeEntry()
                    entry = zip.nextEntry
                    continue
                }
                if (entry.isDirectory) {
                    target.mkdirs()
                } else {
                    target.parentFile?.mkdirs()
                    target.outputStream().use { output -> zip.copyTo(output) }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        true
    } catch (_: Exception) {
        false
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes / 1024.0
    var unit = units[0]
    for (next in units.drop(1)) {
        if (value < 1024) break
        value /= 1024
        unit = next
    }
    return String.format(Locale.getDefault(), "%.1f %s", value, unit)
}
