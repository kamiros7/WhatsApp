package com.camilo.teste.whatsapp.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode){


        // API 23 é a marshmallow, que é a versão do android que começou a implementar o novo sistema de permissões
        if(Build.VERSION.SDK_INT >= 23){

            List<String> listaPermissoes = new ArrayList<>();
            //Percorre as permissões passadas uma a uma
            for(String permissao : permissoes){
               Boolean temPermissao = ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;
               if(!temPermissao) {
                   listaPermissoes.add(permissao);
               }
            }

               //Caso a lista esteja vazia, não é necessario solicitar permissao
                if(listaPermissoes.isEmpty()) {
                    return true;
                }else {
                    //Solicita permissao
                    String[] novasPermissoes = new String[listaPermissoes.size()];
                    listaPermissoes.toArray(novasPermissoes);

                    ActivityCompat.requestPermissions(activity, novasPermissoes, requestCode);
                }
        }
        return true;
    }
}
