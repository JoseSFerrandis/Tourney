package com.example.tourney.tools

import android.util.Patterns
import android.widget.Toast

object CheckValues {
    fun checkEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Comprueba que la contraseña cumple con los requisitos mínimos de seguridad
     * @param password La contraseña a comprobar
     * @return 0 si la contraseña es válida, otro valor en caso contrario
     * 1: La contraseña es demasiado corta
     * 2: La contraseña no contiene al menos una mayúscula
     * 3: La contraseña no contiene al menos un número
     * 4: La contraseña no contiene al menos una minúscula
     * 5: La contraseña no contiene al menos un carácter especial
     * 6: La contraseña contiene espacios
     * 7: La contraseña está vacía
     */
    fun checkPassword(password: String): Int {
        //val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
        if(password.length < 8){ return 1 }
        if(password.isEmpty()){ return 7 }
        if(!password.matches(Regex(".*[A-Z].*"))){ return 2 }
        if(!password.matches(Regex(".*[0-9].*"))){ return 3 }
        if(!password.matches(Regex(".*[a-z].*"))){ return 4 }
        if(!password.matches(Regex(".*[!@#$%^&*()].*"))){ return 5 }
        if(password.matches(Regex(".*\\s.*"))){ return 6 }
        return 0
    }
}