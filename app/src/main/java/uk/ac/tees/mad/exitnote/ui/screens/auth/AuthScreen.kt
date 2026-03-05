package uk.ac.tees.mad.exitnote.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.ac.tees.mad.exitnote.viewmodel.ExitNoteViewModel

/**
 * Authentication Screen - Sign in or Sign up
 * Supports email/password and Google authentication
 */
@Composable
fun AuthScreen(
    viewModel: ExitNoteViewModel,
    onNavigateToSetup: (String?) -> Unit,
    onNavigateToHome: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Exit Note Logo",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF6B7FD7)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSignUp) "Create Account" else "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSignUp)
                    "Sign up to set your home reminder"
                else
                    "Sign in to manage your reminders",
                fontSize = 14.sp,
                color = Color(0xFF7F8C8D),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Email"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B7FD7),
                    unfocusedBorderColor = Color(0xFFBDC3C7)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Hide password"
                            else
                                "Show password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B7FD7),
                    unfocusedBorderColor = Color(0xFFBDC3C7)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (isSignUp) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm Password") },
                    visualTransformation = if (confirmPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible)
                                    Icons.Filled.Visibility
                                else
                                    Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible)
                                    "Hide password"
                                else
                                    "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6B7FD7),
                        unfocusedBorderColor = Color(0xFFBDC3C7)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFE74C3C),
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        email.isBlank() -> errorMessage = "Please enter your email"
                        password.isBlank() -> errorMessage = "Please enter your password"
                        isSignUp && confirmPassword.isBlank() ->
                            errorMessage = "Please confirm your password"
                        isSignUp && password != confirmPassword ->
                            errorMessage = "Passwords do not match"
                        else -> {
                            isLoading = true
                            if (isSignUp) {
                                viewModel.signUpWithEmail(email, password)
                                // TODO: Handle response
                            } else {
                                viewModel.signInWithEmail(email, password)
                                // TODO: Handle response
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B7FD7)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (isSignUp) "Sign Up" else "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSignUp)
                        "Already have an account?"
                    else
                        "Don't have an account?",
                    color = Color(0xFF7F8C8D),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = {
                        isSignUp = !isSignUp
                        errorMessage = null
                        password = ""
                        confirmPassword = ""
                    }
                ) {
                    Text(
                        text = if (isSignUp) "Sign In" else "Sign Up",
                        color = Color(0xFF6B7FD7),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Exit Note – Auth Screen (Sign In)")
@Composable
fun ExitNoteAuthExactPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Exit Note Logo",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF6B7FD7)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in to manage your reminders",
                fontSize = 14.sp,
                color = Color(0xFF7F8C8D),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = "user@example.com",
                onValueChange = {},
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Email"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B7FD7),
                    unfocusedBorderColor = Color(0xFFBDC3C7)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = "••••••••",
                onValueChange = {},
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = "Show password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B7FD7),
                    unfocusedBorderColor = Color(0xFFBDC3C7)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B7FD7)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account?",
                    color = Color(0xFF7F8C8D),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = {}) {
                    Text(
                        text = "Sign Up",
                        color = Color(0xFF6B7FD7),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}