package com.camilo.teste.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camilo.teste.whatsapp.R;
import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.camilo.teste.whatsapp.helper.Base64Custom;
import com.camilo.teste.whatsapp.helper.UsuarioFirebase;
import com.camilo.teste.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText campoNome, campoEmail, campoSenha;
    private Button botaoCadastro;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editPerfilNome);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
        botaoCadastro = findViewById(R.id.botaoCadastrar);
    }

    public void cadastrarUsuario(View view){
        //Recuperando textos
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        int validacao = validarCamposCadastro(textoNome, textoEmail, textoSenha);
        if(validacao == 0){

            final Usuario usuario = new Usuario();
            usuario.setEmail(textoEmail);
            usuario.setNome(textoNome);
            usuario.setSenha(textoSenha);

            firebaseAuth = ConfiguracaoFirebase.getAuth();
            firebaseAuth.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(CadastroActivity.this, "Sucesso ao cadastrar usuário", Toast.LENGTH_SHORT).show();
                                UsuarioFirebase.atualizaNomeUsuario(usuario.getNome());
                                finish();

                                try{
                                    String idUsuario = Base64Custom.codigicarBase64(usuario.getEmail());
                                    usuario.setId(idUsuario);
                                    usuario.salvarUsuario();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            }else{
                                String excecao = "";
                                try{
                                    throw task.getException();
                                }catch (FirebaseAuthWeakPasswordException e){
                                        excecao = "Digite uma senha mais forte! ";
                                }catch (FirebaseAuthInvalidCredentialsException e){
                                    excecao = "Por favor, digite um email válido ";
                                }catch(FirebaseAuthUserCollisionException e){
                                    excecao = "Essa conta já foi cadastrada";
                                }catch(Exception e){
                                    excecao = "Erro ao cadastrar usuário" + e.getMessage();
                                    e.printStackTrace();
                                }

                                Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        else{
            Toast.makeText(CadastroActivity.this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
        }

    }

    public int validarCamposCadastro(String nome, String email,String senha){
        int validacao = 0;
        if(nome.isEmpty() || nome == null){
            validacao++;
        }
        else if(email.isEmpty() || email == null){
            validacao++;
        }
        else if(senha.isEmpty() || senha == null){
            validacao++;
        }
        return validacao;
    }
}
