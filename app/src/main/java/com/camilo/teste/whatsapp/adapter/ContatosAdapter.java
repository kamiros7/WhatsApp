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
import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.camilo.teste.whatsapp.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContatosAdapter extends RecyclerView.Adapter<ContatosAdapter.MyViewHolder> {

    private List<Usuario> contatos;
    private Context context;


    public ContatosAdapter(List<Usuario> listaContatos, Context c){
        this.contatos = listaContatos;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contatos, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario usuario = contatos.get(position);
        boolean itemGrupo = usuario.getEmail().isEmpty();

        holder.nome.setText(usuario.getNome());
        holder.email.setText(usuario.getEmail());

        if(usuario.getFoto() != null ){
            Uri url = Uri.parse(usuario.getFoto());
            Glide.with(context).load(url).into(holder.foto);
        }else{
            if(itemGrupo){
                holder.foto.setImageResource(R.drawable.icone_grupo);
                holder.email.setVisibility(View.GONE); //para não ficar visivel
            }else{
                holder.foto.setImageResource(R.drawable.padrao);
            }

        }
    }

    @Override
    public int getItemCount() {
        return contatos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome, email;

        public MyViewHolder(View itemView){
            super(itemView);

            foto = itemView.findViewById(R.id.imageContato);
            nome = itemView.findViewById(R.id.textNomeContato);
            email = itemView.findViewById(R.id.textEmailContato);

        }
    }

}
