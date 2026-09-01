package com.example.features.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Screen Mode: 0 = Sign In, 1 = Sign Up, 2 = Forgot Password, 3 = Quick Demo Login
    var authMode by remember { mutableIntStateOf(0) }

    // Sign In State
    var emailOrId by remember { mutableStateOf("bodduteja2021@gmail.com") }
    var password by remember { mutableStateOf("teja2021") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Sign Up State
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regInstitution by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("Genomics Investigator") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(true) }

    // Forgot Password State
    var resetEmail by remember { mutableStateOf("") }
    var isResetSent by remember { mutableStateOf(false) }

    // UI Feedback States
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Demo Accounts list for instant 1-tap testing
    val demoAccounts = listOf(
        RegisteredAccount(
            fullName = "Dr. Boddu Teja",
            email = "bodduteja2021@gmail.com",
            phone = "+91 9848012345",
            institution = "Department of Bioinformatics & Genomics",
            role = "Principal Genomics Investigator",
            passKey = "teja2021"
        ),
        RegisteredAccount(
            fullName = "Dr. Sarah Chen",
            email = "sarah.chen@stanford.edu",
            phone = "+1 650 555 0192",
            institution = "Stanford Structural Biology Center",
            role = "Lead Cryo-EM Research Scientist",
            passKey = "chen2026"
        ),
        RegisteredAccount(
            fullName = "Alex Mercer, M.Sc.",
            email = "alex.mercer@embl.de",
            phone = "+49 6221 387 0",
            institution = "European Molecular Biology Laboratory",
            role = "Bioinformatics Pipeline Engineer",
            passKey = "embl2026"
        )
    )

    fun handleSignIn() {
        focusManager.clearFocus()
        val trimmedEmail = emailOrId.trim()
        val trimmedPass = password.trim()

        if (trimmedEmail.isEmpty()) {
            errorMessage = "Please enter your email or institutional access ID."
            return
        }
        if (trimmedPass.isEmpty()) {
            errorMessage = "Please enter your password."
            return
        }

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            delay(800) // Simulate secure authentication check
            val existingAcc = AccountRegistry.accounts.find {
                it.email.equals(trimmedEmail, ignoreCase = true) || it.passKey == trimmedPass
            }

            if (existingAcc != null) {
                AccountRegistry.currentActiveAccount = existingAcc
                successMessage = "Authentication successful! Welcome back, ${existingAcc.fullName}."
                isLoading = false
                delay(600)
                onLoginSuccess()
            } else {
                // Register and log in directly for seamless experience
                val newAcc = RegisteredAccount(
                    fullName = if (trimmedEmail.contains("@")) trimmedEmail.substringBefore("@").replace(".", " ").capitalizeWords() else "Researcher User",
                    email = if (trimmedEmail.contains("@")) trimmedEmail else "$trimmedEmail@bioseqhub.org",
                    institution = "Bioinformatics & Genomics Institute",
                    role = "Genomics Investigator",
                    passKey = trimmedPass
                )
                AccountRegistry.accounts.add(newAcc)
                AccountRegistry.currentActiveAccount = newAcc
                successMessage = "New session authorized! Logged in as ${newAcc.fullName}."
                isLoading = false
                delay(600)
                onLoginSuccess()
            }
        }
    }

    fun handleSignUp() {
        focusManager.clearFocus()
        if (regFullName.isBlank()) {
            errorMessage = "Please enter your full name."
            return
        }
        if (regEmail.isBlank() || !regEmail.contains("@")) {
            errorMessage = "Please enter a valid academic/institutional email address."
            return
        }
        if (regPassword.length < 6) {
            errorMessage = "Password must be at least 6 characters long."
            return
        }
        if (regPassword != regConfirmPassword) {
            errorMessage = "Passwords do not match."
            return
        }
        if (!acceptTerms) {
            errorMessage = "Please accept the Research Data Use Agreement."
            return
        }

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            delay(1000)
            val newAcc = RegisteredAccount(
                fullName = regFullName.trim(),
                email = regEmail.trim(),
                institution = regInstitution.ifBlank { "Department of Structural Genomics" },
                role = regRole.trim(),
                passKey = regPassword.trim()
            )
            AccountRegistry.accounts.add(newAcc)
            AccountRegistry.currentActiveAccount = newAcc
            isLoading = false
            successMessage = "Researcher account created successfully! Welcome, ${newAcc.fullName}."
            delay(700)
            onLoginSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🧬 BioSeq Hub",
                            fontWeight = FontWeight.Bold,
                            color = HighDensityNavy,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = HighDensityPeriwinkle,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "AUTH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = HighDensityNavy,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("login_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HighDensityNavy
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            AccountRegistry.currentActiveAccount = null
                            onLoginSuccess()
                        },
                        modifier = Modifier.testTag("login_guest_skip_button")
                    ) {
                        Text(
                            text = "Guest Mode",
                            fontWeight = FontWeight.Bold,
                            color = HighDensityAccentBlue,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HighDensityNavBg)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(HighDensityCanvas, Color(0xFFF0F4F8))
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner & BioSeq Hub Identity
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = HighDensityNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(HighDensityPeriwinkle.copy(alpha = 0.2f))
                            .border(2.dp, HighDensityPeriwinkle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧬", fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Genomics Research Portal",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Single Sign-On for NCBI, UniProt, Cloud Sheets & Workspace",
                        fontSize = 12.sp,
                        color = HighDensityPeriwinkle,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Mode Selector Tabs (Sign In / Register / Quick Demo)
            TabRow(
                selectedTabIndex = authMode,
                containerColor = Color.White,
                contentColor = HighDensityNavy,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
                    .testTag("auth_mode_tabs")
            ) {
                Tab(
                    selected = authMode == 0,
                    onClick = {
                        authMode = 0
                        errorMessage = null
                    },
                    modifier = Modifier.testTag("tab_signin")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Tab(
                    selected = authMode == 1,
                    onClick = {
                        authMode = 1
                        errorMessage = null
                    },
                    modifier = Modifier.testTag("tab_signup")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Register",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Tab(
                    selected = authMode == 3,
                    onClick = {
                        authMode = 3
                        errorMessage = null
                    },
                    modifier = Modifier.testTag("tab_demo")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "1-Tap Demo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Error / Success Feedback Banner
            AnimatedVisibility(visible = errorMessage != null || successMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (errorMessage != null) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (errorMessage != null) Color(0xFFFCA5A5) else Color(0xFF86EFAC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (errorMessage != null) Icons.Default.Error else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (errorMessage != null) Color(0xFFDC2626) else Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: successMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (errorMessage != null) Color(0xFF991B1B) else Color(0xFF166534),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                errorMessage = null
                                successMessage = null
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // =================================================================
            // MODE 0: SIGN IN FORM
            // =================================================================
            if (authMode == 0) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Sign in to BioSeq Hub",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityNavy
                        )

                        // Email Field
                        OutlinedTextField(
                            value = emailOrId,
                            onValueChange = {
                                emailOrId = it
                                errorMessage = null
                            },
                            label = { Text("Academic Email or Institutional ID") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = HighDensityNavy)
                            },
                            trailingIcon = {
                                if (emailOrId.isNotEmpty()) {
                                    IconButton(onClick = { emailOrId = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear text", tint = Color.Gray)
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password / Passkey") },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null, tint = HighDensityNavy)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { handleSignIn() }),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        // Remember Me & Forgot Password Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { rememberMe = !rememberMe }
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = HighDensityNavy)
                                )
                                Text(
                                    text = "Remember me",
                                    fontSize = 12.sp,
                                    color = HighDensityNavy
                                )
                            }

                            TextTextButton(
                                text = "Forgot password?",
                                onClick = {
                                    authMode = 2
                                    resetEmail = emailOrId
                                    errorMessage = null
                                }
                            )
                        }

                        // Submit Sign In Button
                        Button(
                            onClick = { handleSignIn() },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityNavy),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Verifying Credentials...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = HighDensityBorder
                        )

                        // Google & SSO Login Options
                        Text(
                            text = "OR SIGN IN WITH RESEARCH ENTERPRISE SSO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = {
                                val googleAccount = demoAccounts.first()
                                AccountRegistry.currentActiveAccount = googleAccount
                                successMessage = "Google Research Account linked: ${googleAccount.email}"
                                coroutineScope.launch {
                                    delay(500)
                                    onLoginSuccess()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("login_google_sso_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌐", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Continue with Google Workspace",
                                    fontWeight = FontWeight.SemiBold,
                                    color = HighDensityNavy,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val orcidAcc = RegisteredAccount(
                                    fullName = "Dr. Boddu Teja (ORCID: 0000-0002-1825-0097)",
                                    email = "bodduteja2021@gmail.com",
                                    institution = "National Genomics Data Center",
                                    role = "Principal Genomics Investigator",
                                    passKey = "orcid2026"
                                )
                                AccountRegistry.currentActiveAccount = orcidAcc
                                successMessage = "ORCID iD Verified: 0000-0002-1825-0097"
                                coroutineScope.launch {
                                    delay(500)
                                    onLoginSuccess()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("login_orcid_sso_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔬", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign in with ORCID / NCBI iD",
                                    fontWeight = FontWeight.SemiBold,
                                    color = HighDensityNavy,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // =================================================================
            // MODE 1: REGISTER NEW ACCOUNT
            // =================================================================
            if (authMode == 1) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Register Researcher Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityNavy
                        )

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Full Name & Title (e.g., Dr. Boddu Teja)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = HighDensityNavy) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_fullname_input")
                        )

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Academic Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = HighDensityNavy) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_email_input")
                        )

                        OutlinedTextField(
                            value = regInstitution,
                            onValueChange = { regInstitution = it },
                            label = { Text("Institution / Department") },
                            placeholder = { Text("e.g., Department of Bioinformatics & Genomics") },
                            leadingIcon = { Icon(Icons.Default.Domain, contentDescription = null, tint = HighDensityNavy) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_institution_input")
                        )

                        OutlinedTextField(
                            value = regRole,
                            onValueChange = { regRole = it },
                            label = { Text("Research Role") },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = HighDensityNavy) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_role_input")
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Create Password (min 6 chars)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = HighDensityNavy) },
                            trailingIcon = {
                                IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isRegPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_pass_input")
                        )

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = HighDensityNavy) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_conf_pass_input")
                        )

                        // Terms Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { acceptTerms = !acceptTerms }
                        ) {
                            Checkbox(
                                checked = acceptTerms,
                                onCheckedChange = { acceptTerms = it },
                                colors = CheckboxDefaults.colors(checkedColor = HighDensityNavy)
                            )
                            Text(
                                text = "I accept the BioSeq Hub Genomic Data Security & Cloud Storage Policy.",
                                fontSize = 11.sp,
                                color = HighDensityNavy
                            )
                        }

                        Button(
                            onClick = { handleSignUp() },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityNavy),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("reg_submit_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Creating Profile...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.HowToReg, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Account & Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // =================================================================
            // MODE 2: FORGOT PASSWORD
            // =================================================================
            if (authMode == 2) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Reset Researcher Password",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityNavy
                        )

                        Text(
                            text = "Enter your registered academic or institutional email address to receive a secure password reset key.",
                            fontSize = 12.sp,
                            color = HighDensityTextSecondary
                        )

                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Registered Email") },
                            leadingIcon = { Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = HighDensityNavy) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_email_input")
                        )

                        Button(
                            onClick = {
                                if (resetEmail.contains("@")) {
                                    isResetSent = true
                                    successMessage = "Password reset instructions sent to $resetEmail"
                                } else {
                                    errorMessage = "Please enter a valid email address."
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityNavy),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Reset Link", fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { authMode = 0 },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Back to Sign In", color = HighDensityAccentBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // =================================================================
            // MODE 3: 1-TAP DEMO RESEARCHER LOGIN
            // =================================================================
            if (authMode == 3) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Select Pre-Configured Research Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityNavy
                        )

                        Text(
                            text = "Tap any scientist profile below for instant authentication and immediate access to all 15 research modules.",
                            fontSize = 12.sp,
                            color = HighDensityTextSecondary
                        )

                        demoAccounts.forEach { acc ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = HighDensityCanvas,
                                border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        AccountRegistry.currentActiveAccount = acc
                                        successMessage = "Instant Login as ${acc.fullName}"
                                        coroutineScope.launch {
                                            delay(400)
                                            onLoginSuccess()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(HighDensityPeriwinkle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = acc.fullName.split(" ").takeLast(2).joinToString("") { it.take(1) },
                                            fontWeight = FontWeight.Bold,
                                            color = HighDensityNavy,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = acc.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = HighDensityNavy
                                        )
                                        Text(
                                            text = acc.role,
                                            fontSize = 12.sp,
                                            color = HighDensityTextSecondary
                                        )
                                        Text(
                                            text = acc.institution,
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Login",
                                        tint = HighDensityNavy
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Footer info
            Text(
                text = "BioSeq Hub v1.0 • Encrypted Genomic Data Exchange Protocol",
                fontSize = 11.sp,
                color = HighDensityTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TextTextButton(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = HighDensityAccentBlue,
        modifier = Modifier.clickable { onClick() }
    )
}

private fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    BioSeqHubTheme {
        LoginScreen(onLoginSuccess = {}, onNavigateBack = {})
    }
}
