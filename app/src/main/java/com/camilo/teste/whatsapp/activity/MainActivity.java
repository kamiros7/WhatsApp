package com.camilo.teste.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.camilo.teste.whatsapp.R;
import com.camilo.teste.whatsapp.config.ConfiguracaoFirebase;
import com.camilo.teste.whatsapp.fragment.ContatosFragment;
import com.camilo.teste.whatsapp.fragment.ConversasFragment;
import com.camilo.teste.whatsapp.model.Conversas;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private MaterialSearchView materialSearchPrincipal;
    private FragmentPagerItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = ConfiguracaoFirebase.getAuth();
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("WhatsApp");
        setSupportActionBar(toolbar);

        //Configurar abas
         adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                .add("Conversas", ConversasFragment.class)
                .add("Contatos", ContatosFragment.class)
                .create()
        );

        final ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout smartTabLayout = findViewById(R.id.viewPagerTab);
        smartTabLayout.setViewPager(viewPager);

        //configuracao do id para barra de pesquisa
        materialSearchPrincipal = findViewById(R.id.materialSearchPrincipal);

        //Listner para o search view
        materialSearchPrincipal.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                ConversasFragment fragment = (ConversasFragment)adapter.getPage(0);
                fragment.recarregarConversas();
            }
        });

        //Listner para a caixa de texto
        materialSearchPrincipal.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                switch (viewPager.getCurrentItem()){
                    case 0:
                        ConversasFragment conversasFragment =  (ConversasFragment) adapter.getPage(0);
                        if(newText != null && !newText.isEmpty()){
                            //passo para minusculo, para fim de facilitar a pesquisa
                            conversasFragment.pesquisarConversas(newText.toLowerCase());
                        }else{
                            conversasFragment.recarregarConversas();
                        }
                        break;
                    case 1:
                        ContatosFragment contatosFragment =  (ContatosFragment) adapter.getPage(1);
                        if(newText != null && !newText.isEmpty()){
                            //passo para minusculo, para fim de facilitar a pesquisa
                            contatosFragment.pesquisarContatos(newText.toLowerCase());
                        }else{
                            contatosFragment.recarregarContatos();
                        }
                        break;
                }

                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //configurar botao de pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisa);
        materialSearchPrincipal.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSair :
                deslogarUsuario();
                finish();
                break;

            case R.id.menuConfiguracoes :
                abrirConfiguracoes();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){
        try{
            firebaseAuth.signOut();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void abrirConfiguracoes(){
        Intent intent = new Intent(MainActivity.this, ConfiguracoesActivity.class);
        startActivity(intent);
    }
}
