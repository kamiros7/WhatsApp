package com.camilo.teste.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.camilo.teste.whatsapp.R;
import com.camilo.teste.whatsapp.helper.UsuarioFirebase;
import com.camilo.teste.whatsapp.model.Mensagem;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class MensagensAdapter extends RecyclerView.Adapter<MensagensAdapter.MyViewHolder> {

    private List<Mensagem> mensagens;
    private Context context;
    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;

    public MensagensAdapter(List<Mensagem> m, Context c) {
        this.mensagens = m;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = null;
        if(viewType == TIPO_REMETENTE){
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_remetente, parent, false);
        }else if(viewType == TIPO_DESTINATARIO){
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_destinatario, parent, false);
        }

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Mensagem mensagem = mensagens.get(position);
        String textoMensagem = mensagem.getMensagem();
        String imagemMensagem = mensagem.getImagem();

        if(imagemMensagem != null){
            Uri url = Uri.parse(imagemMensagem);
            Glide.with(context).load(url).into(holder.foto);

            String nome = mensagem.getNome();
            //Nesse caso o nome é do usuario que manda a mensagem, porém só é setado o nome para grupo, conversa entre
            //dois usuarios, o nome está vazio
            if(! nome.isEmpty()){
                holder.nome.setText(nome);
            }else{
                holder.nome.setVisibility(View.GONE);
            }

            //Esconder o texto
            holder.textMensagem.setVisibility(View.GONE);
        }else{
            holder.textMensagem.setText(textoMensagem);

            String nome = mensagem.getNome();
            //Nesse caso o nome é do usuario que manda a mensagem, porém só é setado o nome para grupo, conversa entre
            //dois usuarios, o nome está vazio
            if(! nome.isEmpty()){
                holder.nome.setText(nome);
            }else{
                holder.nome.setVisibility(View.GONE);
            }


            //Esconder imagem
            holder.foto.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    @Override
    public int getItemViewType(int position) {

        //Nesse caso, pego o id do Usuario logado e comparo com o usuario da mensagem ( a mensagem guarda do id do usuario remetente)
        String idUsuario = UsuarioFirebase.getIdUsuario();
        Mensagem mensagem = mensagens.get(position);
        if(idUsuario.equals(mensagem.getIdUsuario())){
            return TIPO_REMETENTE;
        }

        return TIPO_DESTINATARIO;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textMensagem;
        TextView nome;
        ImageView foto;

        public MyViewHolder(View itemView)
        {
            super (itemView);
            textMensagem = itemView.findViewById(R.id.textMensagemTexto);
            foto = itemView.findViewById(R.id.imageMensagemFoto);
            nome = itemView.findViewById(R.id.textNomeExibicao);
        }
    }

}
