package com.example.appbohemia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.security.Principal;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private EditText numero,codigo;
    private Button enviarnumero, enviarcodigo;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String VerificacionID;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth auth;

    private String phoneNumber;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        numero = (EditText) findViewById(R.id.numero);

        codigo = (EditText) findViewById(R.id.codigo);

        enviarnumero = (Button) findViewById(R.id.enviarnumero);

        enviarcodigo = (Button) findViewById(R.id.enviarcodigo);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);




        enviarnumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = numero.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(LoginActivity.this,"Ingresa tu numero primero....", Toast.LENGTH_SHORT).show();


                } else {


                    dialog.setTitle("Validando numero");
                    dialog.setMessage("Por favor espere mientras validamos");
                    dialog.show();

                    dialog.setCanceledOnTouchOutside(true);

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(LoginActivity.this)
                            .setCallbacks(callbacks).build();

                    PhoneAuthProvider.verifyPhoneNumber(options);//Envia el numero..



                    //dialog.setMessage();
                }
            }
        });
    enviarcodigo.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            numero.setVisibility(View.GONE);
            enviarnumero.setVisibility(View.GONE);
            String VerificacionCode = codigo.getText().toString();
            if (TextUtils.isEmpty(VerificacionCode)){
                Toast.makeText(LoginActivity.this, "Ingresa el codigo recibido", Toast.LENGTH_SHORT).show();

            }else {
                dialog.setTitle("Verificando...");
                dialog.setMessage("Espere por favor");
                dialog.show();
                dialog.setCanceledOnTouchOutside(true);
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VerificacionID,VerificacionCode);
                IngresadoConExito(credential);

            }
        }
    });

    callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            IngresadoConExito(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            dialog.dismiss();
            Toast.makeText(LoginActivity.this, "Fallo el Inicio causas\n1.Numero Invalido\n" +
                    "2.Sin conexión", Toast.LENGTH_SHORT).show();
            numero.setVisibility(View.VISIBLE);
            enviarnumero.setVisibility(View.VISIBLE);
            codigo.setVisibility(View.GONE);
            enviarcodigo.setVisibility(View.GONE);

        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            VerificacionID = s;
            resendingToken =token;
            dialog.dismiss();
            Toast.makeText(LoginActivity.this,"codigo enviado satisfactoriamente, revisa su bandeja de entrada",Toast.LENGTH_SHORT).show();
            numero.setVisibility(View.GONE);
            enviarnumero.setVisibility(View.GONE);
            codigo.setVisibility(View.VISIBLE);
            enviarcodigo.setVisibility(View.VISIBLE);


        }
    };




    }
    private void IngresadoConExito(PhoneAuthCredential credential){
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this,"Ingresado con exito", Toast.LENGTH_SHORT).show();
                    EnviarlaPrincipal();
                }else{
                    String err = task.getException().toString();
                    Toast.makeText(LoginActivity.this,"error"+err,Toast.LENGTH_SHORT).show();

                }
            }
        });
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser= auth.getCurrentUser();
        if(firebaseUser !=null){
        EnviarlaPrincipal();
        }
    }

    private void EnviarlaPrincipal() {
        Intent intent= new Intent(LoginActivity.this, PrincipalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("phone",phoneNumber);
        startActivity(intent);
        finish();
    }
}