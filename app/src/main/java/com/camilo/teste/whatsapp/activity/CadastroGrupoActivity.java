package com.camilo.teste.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.camilo.teste.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.camilo.teste.whatsapp.helper.UsuarioFirebase;
import com.camilo.teste.whatsapp.model.Grupo;
import com.camilo.teste.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.camilo.teste.whatsapp.R;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private TextView textTotalParticipantes;
    private CircleImageView imageGrupo;
    private FloatingActionButton fabCadastrarGrupo;
    private EditText editNomeGrupo;

    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private RecyclerView recyclerMembrosGrupo;

    private Grupo grupo;

    private static final int SELECAO_GALERIA = 200;

    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        toolbar.setSubtitle("Defina um nome");
        setSupportActionBar(toolbar);

        //Configurações iniciais
        textTotalParticipantes = findViewById(R.id.textTotalParticipantes);
        recyclerMembrosGrupo= findViewById(R.id.recyclerMembrosGrupo);
        imageGrupo = findViewById(R.id.imagemGrupo);
        storageReference = ConfiguracaoFirebase.getStorageReference();
        fabCadastrarGrupo = findViewById(R.id.fabSalvarGrupo);
        editNomeGrupo = findViewById(R.id.editNomeGrupo);

        /*Crio uma instância de grupo, pois no construtor do Grupo, eu pego o Id do firebase e coloco no id do grupo, sendo assim
          cada grupo criado, antes mesmo do usuário usar a activity de cadastro, o grupo terá seu id e será único, visto que toda vez
          que a activity é criada, um novo grupo é instanciado */

        grupo = new Grupo();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recuperando lista de usuarios passada pelo grupoActivity
        if(getIntent().getExtras() != null){
            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            listaMembrosSelecionados.addAll(membros);
            textTotalParticipantes.setText("Participantes: " + listaMembrosSelecionados.size());
        }

        //Configurando o recycler view para membros participantes do grupo
        //configurando o adapter
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());

        //configurando o recycler view
        RecyclerView.LayoutManager layoutManagerGrupoSelecionado = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerMembrosGrupo.setLayoutManager(layoutManagerGrupoSelecionado);
        recyclerMembrosGrupo.setHasFixedSize(true);
        recyclerMembrosGrupo.setAdapter(grupoSelecionadoAdapter);

        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //segundo argumento é o argumento padrao do aramzenamento do celular
                Intent i =  new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                //Forma de verificar que abrir a nova activity ( no caso a galeria) foi feita com sucesso
                if(i.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(i,SELECAO_GALERIA );
            }
        });

        fabCadastrarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeGrupo = editNomeGrupo.getText().toString();

                //adicionando o membro logado a lista, para ser evniada para o grupo (pois o membro é indiretamente selecionado)
                listaMembrosSelecionados.add(UsuarioFirebase.getDadosUsuarioLogado());
                grupo.setMembros(listaMembrosSelecionados);
                grupo.setNome(nomeGrupo);
                grupo.salvar();

                Intent i = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                i.putExtra("chatGrupo", grupo);
                startActivity(i);

              /* Pensar numa forma de sair dessa activity e ir para chat activity certa */
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //verifica se o dado requirido foi obtido com sucesso ( a foto da galeria ou da camera, no caso)
        if(resultCode == RESULT_OK){
            Bitmap image = null;
            try{
                //Nesse caso, haverá apenas a seleção da galeria
                Uri localImageSelecionada = data.getData();
                image = MediaStore.Images.Media.getBitmap(getContentResolver(),localImageSelecionada);

                if(image != null){
                    imageGrupo.setImageBitmap(image);

                    //Recuperar dados da imagem para salver no firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImage = baos.toByteArray();

                    //Salvar referencia da imagem no firebase
                    final StorageReference imageRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId() + ".jpeg");

                    //Salvar imagem no Storage do firebase
                    UploadTask uploadTask = imageRef.putBytes(dadosImage);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CadastroGrupoActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CadastroGrupoActivity.this, "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String url = task.getResult().toString();
                                    grupo.setFoto(url);

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

}
