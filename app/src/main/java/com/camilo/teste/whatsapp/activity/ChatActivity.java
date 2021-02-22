package com.camilo.teste.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.camilo.teste.whatsapp.adapter.MensagensAdapter;
import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.camilo.teste.whatsapp.helper.Base64Custom;
import com.camilo.teste.whatsapp.helper.UsuarioFirebase;
import com.camilo.teste.whatsapp.model.Conversas;
import com.camilo.teste.whatsapp.model.Grupo;
import com.camilo.teste.whatsapp.model.Mensagem;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.camilo.teste.whatsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageFotoChat;
    private EditText editTextMensagem;
    private ImageView imageGaleria, imageCamera;

    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private Grupo grupo;
    private RecyclerView recyclerViewMensagens;
    private List<Mensagem> mensagens = new ArrayList<>();
    private MensagensAdapter adapter;

    private DatabaseReference databaseRef;
    private StorageReference storageRef;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;

    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    //identificador usuario remetente e destinatario
    private String idUsuarioDestinatario, idUsuarioRemetente;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);

        //configurando titulo vazio para a toolbar
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageFotoChat = findViewById(R.id.circleImageFotoChat);
        editTextMensagem = findViewById(R.id.editTextMensagem);
        recyclerViewMensagens = findViewById(R.id.recyclerViewMensagens);
        imageCamera = findViewById(R.id.imageViewCameraChat);
        imageGaleria = findViewById(R.id.imageViewGaleriaChat);

        //recupera o id do usuario que manda a mensagem ( o que está logado)
        idUsuarioRemetente = UsuarioFirebase.getIdUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

        //Recuperar os dados do usuario destinatario, vinda através do contatos fragment
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            //É preciso fazer a verificação que tipo de chave é, pois cada getSerializable retorna um tipo diferente
            //Pois um retorna o grupo o outro retorna um usuario, tudo isso para setar o nome/foto do chat
            if(bundle.containsKey("chatGrupo")){
                /**************Chat para conversa em grupo*****************/
                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                textViewNome.setText(grupo.getNome());
                idUsuarioDestinatario = grupo.getId();

                String foto = grupo.getFoto();

                if(foto != null || foto == ""){
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this).load(url).into(circleImageFotoChat);
                }else{
                    circleImageFotoChat.setImageResource(R.drawable.padrao);
                }
            }else{
                /******Chat para conversa com usuario*******/
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNome.setText(usuarioDestinatario.getNome());

                String foto = usuarioDestinatario.getFoto();

                if(foto != null || foto == ""){
                    Uri url = Uri.parse(usuarioDestinatario.getFoto());

                    Glide.with(ChatActivity.this).load(url).into(circleImageFotoChat);
                }else{
                    circleImageFotoChat.setImageResource(R.drawable.padrao);
                }

                //recuperar o id do usuario destinatario ( que esta recebendo a amensagem)
                idUsuarioDestinatario = Base64Custom.codigicarBase64(usuarioDestinatario.getEmail());
                /*************************/
            }

        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //configurar o adapter
        adapter = new MensagensAdapter(mensagens, getApplicationContext());
        //configurar o recycler view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewMensagens.setLayoutManager(layoutManager);
        recyclerViewMensagens.setHasFixedSize(true);
        recyclerViewMensagens.setAdapter(adapter);

        //configurando a referencia do database para o nó de mensagens
        databaseRef = ConfiguracaoFirebase.getDatabase();
        storageRef = ConfiguracaoFirebase.getStorageReference();
        mensagensRef = databaseRef.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //verifica se o dado requirido foi obtido com sucesso ( a foto da galeria ou da camera, no caso)
        if(resultCode == RESULT_OK) {
            Bitmap image = null;
            try{
                switch (requestCode) {
                    case SELECAO_CAMERA:
                        image = (Bitmap) data.getExtras().get("data");
                        break;

                    case SELECAO_GALERIA:
                        Uri localImageSelecionada = data.getData();
                        image = MediaStore.Images.Media.getBitmap(getContentResolver(), localImageSelecionada);
                        break;
                }

                if(image != null){
                    //Recuperar dados da imagem para salver no firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImage = baos.toByteArray();

                    //Criar nome da imagem
                    String nomeImagem = UUID.randomUUID().toString();

                    //Configurar referencia para o storage
                    final StorageReference imageRef = storageRef.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);

                    //Salvar imagem no Storage do firebase
                    UploadTask uploadTask = imageRef.putBytes(dadosImage);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ChatActivity.this, "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String downloadUrl = task.getResult().toString();

                                    if(usuarioDestinatario != null){
                                        Mensagem msg = new Mensagem();
                                        msg.setIdUsuario(idUsuarioRemetente);
                                        msg.setMensagem("imagem.jpeg");
                                        msg.setImagem(downloadUrl);

                                        //salvar mensagem para remetente
                                        salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, msg);

                                        //salvar mensagem para destinatario
                                        salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, msg);
                                    }else{
                                        for(Usuario membro : grupo.getMembros()){
                                            String idDestinatario = Base64Custom.codigicarBase64(membro.getEmail()); //id do membro
                                            String idRemetente = UsuarioFirebase.getIdUsuario();

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario(idRemetente);
                                            mensagem.setNome(usuarioRemetente.getNome());
                                            mensagem.setMensagem("imagem.jpeg");
                                            mensagem.setImagem(downloadUrl);

                                            //idUsuarioDestinatario nesse caso está o id do grupo

                                            //Nesse caso foi tratado como se cada membro(destinatario) mandassa a mensagem pro grupo e o grupo é o destinatario
                                            //idUsuarioDestinatario, como é para grupo, ele é configurado com o id do grupo
                                            salvarMensagem(idDestinatario,idUsuarioDestinatario,mensagem);

                                            //salvar conversa
                                            salvarConversa(idDestinatario, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);
                                        }
                                    }

                                    Toast.makeText(ChatActivity.this, "Sucesso ao enviar imagem", Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    });
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public  void enviarMensagem(View view){
        String textoMensagem = editTextMensagem.getText().toString();

        if(!textoMensagem.isEmpty()){

            //quando há usuarioDestinatario, é porque é uma conversa normal, senão é uma conversa de gurpo
            if(usuarioDestinatario != null){

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                //mensagem enviado pelo remetente
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                //mensagem enviado pelo destinatario (destinatario torna o remetente)
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                //salvar conversa remetente
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false);

                //salvar conversa destinatario
                Usuario usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false);
            }else{
                for(Usuario membro : grupo.getMembros()){
                    String idDestinatario = Base64Custom.codigicarBase64(membro.getEmail()); //id do membro
                    String idRemetente = UsuarioFirebase.getIdUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idRemetente);
                    mensagem.setNome(usuarioRemetente.getNome());
                    mensagem.setMensagem(textoMensagem);

                    //idUsuarioDestinatario nesse caso está o id do grupo

                    //Nesse caso foi tratado como se cada membro(destinatario) mandassa a mensagem pro grupo e o grupo é o destinatario
                    //idUsuarioDestinatario, como é para grupo, ele é configurado com o id do grupo
                    salvarMensagem(idDestinatario,idUsuarioDestinatario,mensagem);

                    //salvar conversa
                    salvarConversa(idDestinatario, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);
                }
            }
        }else{
            Toast.makeText(ChatActivity.this,
                    "Digite uma mensagem para enviar",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void salvarMensagem(String idRemetente, String idDestinatario, Mensagem mensagem){
        DatabaseReference databaseRef = ConfiguracaoFirebase.getDatabase();
        DatabaseReference mensagemRef = databaseRef.child("mensagens");

        mensagemRef.child(idRemetente)   //push cria um nó(filho) com id unico, isso é feito, para que o valor de uma nova mensagem
                .child(idDestinatario)   //não sobreescreva a anterior, logo cada mensagem tem seu identificador
                .push()
                .setValue(mensagem);

        editTextMensagem.setText(""); //após a mensagem ser salva, a caixa de texto de mensagem é limpa
    }

    private void recuperarMensagens(){
        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem mensagem, boolean isGroup){

        Conversas conversa = new Conversas();
        conversa.setIdUsuarioDestinatario(idDestinatario);
        conversa.setIdUsuarioRemetente(idRemetente);
        conversa.setUltimaMensagem(mensagem.getMensagem());

        if(isGroup){
            conversa.setGrupo(grupo);
            conversa.setIsGroup("true");
        }else{

            conversa.setUsuario(usuarioExibicao);
            conversa.setIsGroup("false");
        }
        conversa.salvar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //faço um clear da lista de mensagens, para que quando troca a activity, não duplique ( esse caso acontecia com as fotos já enviadas)
        mensagens.clear();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    public void abrirCamera(View view){
        Intent i =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Forma de verificar que abrir a nova activity ( no caso a camera) foi feita com sucesso
        if(i.resolveActivity(getPackageManager()) != null)
            startActivityForResult(i,SELECAO_CAMERA );
    }

    public void abrirGaleria(View view){
        //segundo argumento é o argumento padrao do aramzenamento do celular
        Intent i =  new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //Forma de verificar que abrir a nova activity ( no caso a galeria) foi feita com sucesso
        if(i.resolveActivity(getPackageManager()) != null)
            startActivityForResult(i,SELECAO_GALERIA );
    }
}
