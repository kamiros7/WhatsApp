package com.camilo.teste.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.camilo.teste.whatsapp.R;
import com.camilo.teste.whatsapp.model.Conversas;
import com.camilo.teste.whatsapp.model.Grupo;
import com.camilo.teste.whatsapp.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversasAdapter extends RecyclerView.Adapter<ConversasAdapter.MyViewHolder> {

    private List<Conversas> conversas;
    private Context context;

    public ConversasAdapter(List<Conversas> listaConversas, Context c){
        this.conversas = listaConversas;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //como o layout do adapter contatos é igual do de conversas, estou chamando diretamente o layout de contatos
        //Mesmo possuindo "ids" com nomes distorcidos, o resultado será o mesmo
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contatos, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Conversas conversa = conversas.get(position);
        holder.ultimaMensagem.setText(conversa.getUltimaMensagem());

        if(conversa.getIsGroup().equals("true")){
            //tratamento de conversa para grupos
            Grupo grupo = conversa.getGrupo();
            holder.nome.setText(grupo.getNome());

            if(grupo.getFoto() != null){
                Uri url = Uri.parse(grupo.getFoto());
                Glide.with(context).load(url).into(holder.foto);
            }else{
                holder.foto.setImageResource(R.drawable.padrao);
            }

        }else{
            //tratamento de conversa com usuario
            Usuario usuario = conversa.getUsuario();
            if(usuario != null){
                holder.nome.setText(usuario.getNome());

                if(usuario.getFoto() != null){
                    Uri url = Uri.parse(usuario.getFoto());
                    Glide.with(context).load(url).into(holder.foto);
                }else{
                    holder.foto.setImageResource(R.drawable.padrao);
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return conversas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView foto;
        TextView nome, ultimaMensagem;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.imageContato);
            nome = itemView.findViewById(R.id.textNomeContato);
            ultimaMensagem = itemView.findViewById(R.id.textEmailContato);
        }
    }

    public List<Conversas> getConversas() {
        return conversas;
    }

    public void setConversas(List<Conversas> conversas) {
        this.conversas = conversas;
    }
}
