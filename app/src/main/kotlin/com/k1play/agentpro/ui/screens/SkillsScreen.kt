package com.k1play.agentpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tambah
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.k1play.agentpro.skills.Skill
import com.k1play.agentpro.ui.viewmodel.KeahlianViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeahlianScreen(
    onBack: () -> Unit,
    viewModel: KeahlianViewModel = hiltViewModel()
) {
    val skills by viewModel.skills.collectAsState()
    var showTambahDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keahlian") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showTambahDialog = true }) {
                        Icon(Icons.Default.Tambah, "Tambah Skill")
                    }
                }
            )
        }
    ) { padding ->
        if (skills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        "No skills configured",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Keahlian are reusable workflows the AI can follow.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(skills, key = { it.name }) { skill ->
                    SkillCard(
                        skill = skill,
                        onDelete = { viewModel.removeSkill(skill.name) }
                    )
                }
            }
        }
    }

    if (showTambahDialog) {
        TambahSkillDialog(
            onDismiss = { showTambahDialog = false },
            onTambah = { name, description, trigger, steps ->
                viewModel.addSkill(name, description, trigger, steps)
                showTambahDialog = false
            }
        )
    }
}

@Composable
private fun SkillCard(skill: Skill, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(skill.name, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Text(skill.description, style = MaterialTheme.typography.bodyMedium)
            if (skill.trigger.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Trigger: \"${skill.trigger}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (skill.steps.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                skill.steps.forEachIndexed { i, step ->
                    Text("${i + 1}. $step", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun TambahSkillDialog(
    onDismiss: () -> Unit,
    onTambah: (name: String, description: String, trigger: String, steps: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var trigger by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Keahlian Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("Frasa pemicu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    label = { Text("Langkah (satu per baris)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onTambah(name, description, trigger, steps) },
                enabled = name.isNotBlank()
            ) {
                Text("Tambah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
