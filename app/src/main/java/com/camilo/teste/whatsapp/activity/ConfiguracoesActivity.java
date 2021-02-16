package com.camilo.teste.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.camilo.teste.whatsapp.R;
import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.camilo.teste.whatsapp.helper.Permissao;
import com.camilo.teste.whatsapp.helper.UsuarioFirebase;
import com.camilo.teste.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private ImageButton imgCamera, imgGaleria;
    private CircleImageView circleImageView;
    private EditText campoNome;
    private ImageView imgAtualizarNome;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private String idUsuario;
    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        imgCamera = findViewById(R.id.imageButtonCamera);
        imgGaleria = findViewById(R.id.imageButtonGaleria);
        circleImageView = findViewById(R.id.profile_image);
        campoNome = findViewById(R.id.editPerfilNome);
        imgAtualizarNome = findViewById(R.id.imageAtualizarNome);

        //configurações iniciais
        storageReference = ConfiguracaoFirebase.getStorageReference();
        idUsuario = UsuarioFirebase.getIdUsuario();
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //Recuperar dados do usuario
        FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();

        if(url != null){
            Glide.with(ConfiguracoesActivity.this)
                    .load(url)
                    .into(circleImageView);
        }else{
            circleImageView.setImageResource(R.drawable.padrao);
        }
        campoNome.setText(usuario.getDisplayName());

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                //Forma de verificar que abrir a nova activity ( no caso a camera) foi feita com sucesso
                if(i.resolveActivity(getPackageManager()) != null)
                     startActivityForResult(i,SELECAO_CAMERA );
            }
        });

        imgGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //segundo argumento é o argumento padrao do aramzenamento do celular
                Intent i =  new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                //Forma de verificar que abrir a nova activity ( no caso a galeria) foi feita com sucesso
                if(i.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(i,SELECAO_GALERIA );
            }
        });

        imgAtualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = campoNome.getText().toString();
                boolean retorno = UsuarioFirebase.atualizaNomeUsuario(nome);
                if(retorno){

                    usuarioLogado.setNome(nome);
                    usuarioLogado.atualizar();

                    Toast.makeText(ConfiguracoesActivity.this, "Nome alterado com sucesso", Toast.LENGTH_SHORT).show();

                }
            }
        });

        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);

        /*
        A linha de código abaixo habilita o botão de voltar no layout ( mas é preciso configurar  a ação, assim só está visivel no layout)
        Para que o botão funcione, no arquivo manifest. Na activity que contém o botão, escrever que ela é filha da activity que quero voltar
        No caso, na activity configuracoes, colocar parent para o mainActivity
        */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //verifica se o dado requirido foi obtido com sucesso ( a foto da galeria ou da camera, no caso)
        if(resultCode == RESULT_OK){
            Bitmap image = null;
            try{
                switch (requestCode){
                    case SELECAO_CAMERA :
                        image = (Bitmap) data.getExtras().get("data");
                        break;

                    case SELECAO_GALERIA :
                        Uri localImageSelecionada = data.getData();
                        image = MediaStore.Images.Media.getBitmap(getContentResolver(),localImageSelecionada);
                        break;
                }

                if(image != null){
                    circleImageView.setImageBitmap(image);

                    //Recuperar dados da imagem para salver no firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImage = baos.toByteArray();

                    //Salvar referencia da imagem no firebase
                    final StorageReference imageRef = storageReference
                            .child("imagens")
                            .child("perfil")
                            .child(idUsuario + ".jpeg");

                    //Salvar imagem no Storage do firebase
                    UploadTask uploadTask = imageRef.putBytes(dadosImage);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ConfiguracoesActivity.this, "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    atualizaFotoUsuario(url);
                                }
                            });

                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int resultadoPermissao : grantResults){
            if(resultadoPermissao == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    public void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões negadas");
        builder.setCancelable(false);
        builder.setMessage("Para utilizar o app, é necessário aceitar as permissões");
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void atualizaFotoUsuario(Uri url){
       boolean retorno = UsuarioFirebase.atualizaFotoUsuario(url);
        if(retorno){
            usuarioLogado.setFoto(url.toString());
            usuarioLogado.atualizar();

            Toast.makeText(ConfiguracoesActivity.this,
                    "Sua foto foi alterada com sucesso",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
