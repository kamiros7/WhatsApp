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
import com.camilo.teste.whatsapp.model.Usuario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GrupoSelecionadoAdapter extends RecyclerView.Adapter<GrupoSelecionadoAdapter.MyViewHolder> {

    private List<Usuario> contatosSelecionado;
    private Context context;


    public GrupoSelecionadoAdapter(List<Usuario> listaContatos, Context c){
        this.contatosSelecionado = listaContatos;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grupo_selecionado, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Usuario usuario = contatosSelecionado.get(position);

        holder.nome.setText(usuario.getNome());

        if(usuario.getFoto() != null ){
            Uri url = Uri.parse(usuario.getFoto());
            Glide.with(context).load(url).into(holder.foto);
        }else{
            holder.foto.setImageResource(R.drawable.padrao);

        }
    }

    @Override
    public int getItemCount() {
        return contatosSelecionado.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome;

        public MyViewHolder(View itemView){
            super(itemView);

            foto = itemView.findViewById(R.id.imageMembroSelecionado);
            nome = itemView.findViewById(R.id.textNomeMembroSelecionado);
        }
    }

}
