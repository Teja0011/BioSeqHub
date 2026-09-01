package com.example.features.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

// BioSeq Design Tokens
private val BioSeqAccent = Color(0xFF2563EB)
private val BioSeqNavy = Color(0xFF0F172A)
private val BioSeqLightBlue = Color(0xFFEFF6FF)
private val BioSeqDark = Color(0xFF1E293B)
private val BioSeqGrey = Color(0xFF64748B)
private val BioSeqLightGrey = Color(0xFFF1F5F9)
private val BioSeqGreen = Color(0xFF16A34A)

data class RegisteredAccount(
    val fullName: String,
    val email: String,
    val phone: String = "+91 9876543210",
    val institution: String,
    val role: String,
    val passKey: String,
    val isGoogleLinked: Boolean = true
)

object AccountRegistry {
    val accounts = mutableStateListOf(
        RegisteredAccount(
            fullName = "Dr. Boddu Teja",
            email = "bodduteja2021@gmail.com",
            phone = "+91 9848012345",
            institution = "Department of Bioinformatics & Genomics",
            role = "Principal Genomics Investigator",
            passKey = "teja2021",
            isGoogleLinked = true
        )
    )

    var currentActiveAccount by mutableStateOf<RegisteredAccount?>(accounts.first())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSignInScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeUser by remember { mutableStateOf(AccountRegistry.currentActiveAccount) }
    // Auth screens: 0 = Login, 1 = OTP Verification, 2 = Create Account, 3 = Profile View
    var currentStep by remember { mutableIntStateOf(if (activeUser != null) 3 else 0) }

    // Input States
    var loginInput by remember { mutableStateOf("bodduteja2021@gmail.com") } // Email or Phone
    var passwordInput by remember { mutableStateOf("teja2021") }
    var otpDigits by remember { mutableStateOf(listOf("5", "9", "2", "4", "0", "1")) }
    var otpTimer by remember { mutableIntStateOf(29) }
    var isPhoneMode by remember { mutableStateOf(false) }

    // Registration States
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPass by remember { mutableStateOf("") }
    var regInstitution by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("Genomics Researcher") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var driveAuthorized by remember { mutableStateOf(true) }
    var sheetsAuthorized by remember { mutableStateOf(true) }

    // OTP Timer countdown
    LaunchedEffect(currentStep) {
        if (currentStep == 1) {
            otpTimer = 29
            while (otpTimer > 0) {
                delay(1000)
                otpTimer--
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(BioSeqAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧬", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BioSeq Hub Auth",
                            fontWeight = FontWeight.Bold,
                            color = BioSeqDark,
                            fontSize = 17.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BioSeqDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // BioSeq Hub VIP Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BioSeqNavy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("BIOSEQ", fontWeight = FontWeight.Black, color = BioSeqAccent, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = Color(0xFF38BDF8),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("HUB", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("GENOMICS PASS", color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Authorized laboratory access with unlimited queries & Google Cloud OAuth sync",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = BioSeqAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Error / Warning Banner
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF1F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFF9F1239),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Toast Banner
                if (toastMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BioSeqGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BioSeqGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = toastMessage ?: "",
                                color = Color(0xFF166534),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // VIEW 0: LOGIN SCREEN WITH CREATE ACCOUNT OPTION
                // -------------------------------------------------------------
                if (currentStep == 0) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text("LOGIN", fontSize = 22.sp, fontWeight = FontWeight.Black, color = BioSeqDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("or ", fontSize = 13.sp, color = BioSeqGrey)
                                        Text(
                                            text = "create an account",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BioSeqAccent,
                                            modifier = Modifier.clickable {
                                                errorMessage = null
                                                currentStep = 2
                                            }
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BioSeqLightBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Biotech, contentDescription = null, tint = BioSeqAccent, modifier = Modifier.size(26.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(3.dp)
                                    .background(BioSeqDark)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // One-Tap Quick Login as Dr. Boddu Teja
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = BioSeqLightGrey,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val match = AccountRegistry.accounts.first()
                                        AccountRegistry.currentActiveAccount = match
                                        activeUser = match
                                        currentStep = 3
                                        toastMessage = "Quick Login Successful! Welcome, ${match.fullName}"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(BioSeqNavy),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("BT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Continue as Dr. Boddu Teja", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BioSeqDark)
                                        Text("bodduteja2021@gmail.com • Direct Google Login", fontSize = 11.sp, color = BioSeqGrey)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BioSeqGrey)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                                Text("  OR  ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BioSeqGrey)
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Input Field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPhoneMode) "Mobile Number" else "Academic Email Address",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BioSeqDark
                                )
                                Text(
                                    text = if (isPhoneMode) "Use Email" else "Use Phone",
                                    fontSize = 11.sp,
                                    color = BioSeqAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        isPhoneMode = !isPhoneMode
                                        loginInput = if (isPhoneMode) "9848012345" else "bodduteja2021@gmail.com"
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = loginInput,
                                onValueChange = { loginInput = it },
                                leadingIcon = {
                                    if (isPhoneMode) {
                                        Text("+91  |", fontWeight = FontWeight.Bold, color = BioSeqDark, modifier = Modifier.padding(start = 12.dp))
                                    } else {
                                        Icon(Icons.Default.Email, contentDescription = null, tint = BioSeqGrey)
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = if (isPhoneMode) KeyboardType.Phone else KeyboardType.Email),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_input_field")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Password / Passkey",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BioSeqDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BioSeqGrey) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_field")
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Direct Login CTA Button
                            Button(
                                onClick = {
                                    val trimmed = loginInput.trim()
                                    val match = AccountRegistry.accounts.find {
                                        it.email.equals(trimmed, ignoreCase = true) ||
                                        it.phone.contains(trimmed) ||
                                        it.passKey == passwordInput.trim()
                                    }

                                    if (match != null) {
                                        AccountRegistry.currentActiveAccount = match
                                        activeUser = match
                                        currentStep = 3
                                        toastMessage = "Login Successful! Welcome, ${match.fullName}"
                                    } else {
                                        errorMessage = "Account '$trimmed' not found. Please verify your credentials or tap 'Create Account' below."
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BioSeqNavy),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("login_submit_btn")
                            ) {
                                Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Create Account Secondary Button
                            OutlinedButton(
                                onClick = {
                                    errorMessage = null
                                    currentStep = 2
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BioSeqAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("create_account_btn")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = BioSeqAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CREATE NEW ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BioSeqAccent)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "By continuing, you agree to the BioSeq Hub Terms of Service & Privacy Policy",
                                fontSize = 10.sp,
                                color = BioSeqGrey,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // VIEW 1: 6-DIGIT OTP VERIFICATION
                // -------------------------------------------------------------
                if (currentStep == 1) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("ENTER OTP", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BioSeqDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("We've sent an OTP to $loginInput ", fontSize = 12.sp, color = BioSeqGrey)
                                Text(
                                    "EDIT",
                                    color = BioSeqAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable { currentStep = 0 }
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                otpDigits.forEachIndexed { _, digit ->
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BioSeqLightGrey)
                                            .border(1.5.dp, if (digit.isNotEmpty()) BioSeqAccent else Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = digit,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BioSeqDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (otpTimer > 0) {
                                    Text("Resend OTP in ", fontSize = 12.sp, color = BioSeqGrey)
                                    Text("00:${if (otpTimer < 10) "0$otpTimer" else "$otpTimer"}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BioSeqDark)
                                } else {
                                    Text(
                                        "RESEND OTP",
                                        fontWeight = FontWeight.Bold,
                                        color = BioSeqAccent,
                                        fontSize = 13.sp,
                                        modifier = Modifier.clickable { otpTimer = 29 }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val match = AccountRegistry.accounts.first()
                                    AccountRegistry.currentActiveAccount = match
                                    activeUser = match
                                    currentStep = 3
                                    toastMessage = "OTP Verified! Logged in as ${match.fullName}"
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BioSeqNavy),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("VERIFY AND PROCEED", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // VIEW 2: CREATE ACCOUNT / SIGN UP
                // -------------------------------------------------------------
                if (currentStep == 2) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text("SIGN UP", fontSize = 22.sp, fontWeight = FontWeight.Black, color = BioSeqDark)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("or ", fontSize = 13.sp, color = BioSeqGrey)
                                        Text(
                                            text = "login to your account",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BioSeqAccent,
                                            modifier = Modifier.clickable {
                                                errorMessage = null
                                                currentStep = 0
                                            }
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BioSeqLightBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = BioSeqAccent, modifier = Modifier.size(26.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(3.dp)
                                    .background(BioSeqDark)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it },
                                label = { Text("Full Name (e.g., Dr. Boddu Teja)") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = BioSeqGrey) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_name_field")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Academic / Institutional Email") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BioSeqGrey) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_email_field")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it },
                                label = { Text("Mobile Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BioSeqGrey) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regInstitution,
                                onValueChange = { regInstitution = it },
                                label = { Text("Research Institution / Department") },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = BioSeqGrey) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPass,
                                onValueChange = { regPass = it },
                                label = { Text("Create Passkey / Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BioSeqGrey) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    if (regName.isBlank() || regEmail.isBlank()) {
                                        errorMessage = "Please fill in your name and institutional email address."
                                        return@Button
                                    }
                                    val newAcc = RegisteredAccount(
                                        fullName = regName.trim(),
                                        email = regEmail.trim(),
                                        phone = regPhone.ifBlank { "+91 9848012345" },
                                        institution = regInstitution.ifBlank { "Department of Bioinformatics" },
                                        role = regRole,
                                        passKey = regPass.ifBlank { "pass2026" }
                                    )
                                    AccountRegistry.accounts.add(newAcc)
                                    AccountRegistry.currentActiveAccount = newAcc
                                    activeUser = newAcc
                                    currentStep = 3
                                    toastMessage = "Account created successfully! Welcome, ${newAcc.fullName}"
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BioSeqNavy),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("signup_submit_btn")
                            ) {
                                Text("CREATE ACCOUNT & CONTINUE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // VIEW 3: ACTIVE RESEARCHER PROFILE & GOOGLE OAUTH PERMISSIONS
                // -------------------------------------------------------------
                if (currentStep == 3 && activeUser != null) {
                    val user = activeUser!!
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(BioSeqNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.fullName.split(" ").takeLast(2).joinToString("") { it.take(1) },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.fullName, fontWeight = FontWeight.Black, fontSize = 17.sp, color = BioSeqDark)
                                    Text(user.email, fontSize = 12.sp, color = BioSeqGrey)
                                    Text(user.role, fontSize = 11.sp, color = BioSeqAccent, fontWeight = FontWeight.Bold)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BioSeqGreen)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = BioSeqGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BioSeqGreen)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("CONNECTED GOOGLE SERVICES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BioSeqGrey)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Google Drive Scope
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BioSeqLightGrey,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = BioSeqAccent)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Google Drive Integration", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BioSeqDark)
                                            Text("Store bioinformatics FASTA/FASTQ reports", fontSize = 11.sp, color = BioSeqGrey)
                                        }
                                    }
                                    Switch(
                                        checked = driveAuthorized,
                                        onCheckedChange = { driveAuthorized = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = BioSeqAccent, checkedTrackColor = BioSeqLightBlue)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Google Sheets Scope
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BioSeqLightGrey,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TableChart, contentDescription = null, tint = BioSeqGreen)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Google Sheets Sync", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BioSeqDark)
                                            Text("Export PCR primers & SNP variant tables", fontSize = 11.sp, color = BioSeqGrey)
                                        }
                                    }
                                    Switch(
                                        checked = sheetsAuthorized,
                                        onCheckedChange = { sheetsAuthorized = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = BioSeqAccent, checkedTrackColor = BioSeqLightBlue)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        AccountRegistry.currentActiveAccount = null
                                        activeUser = null
                                        currentStep = 0
                                        toastMessage = "Signed out successfully."
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Switch Account", fontSize = 12.sp, color = BioSeqDark, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = onNavigateBack,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BioSeqNavy),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Back to Hub", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
