<p>Clone do app WhatsApp, perfomando as funções básicas do aplicativo, no qual foi implementado em linguagem Java, utilizando Firebase como banco de dados </p>

<p align="center">
  <a href="#-o-projeto">📓 O Projeto</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#-tecnologias"> 🔧 Tecnologias</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#-funcionalidades">💡 Funcionalidades</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#-como-testar">🧪 Como testar</a>&nbsp;&nbsp;&nbsp;
</p>

## 📓 O Projeto 
<p> Foi criado com o intuito de aprimorar as habildides em desenvolvimento de aplicativos para android de forma nativa, correlacionado-se com o aprendizado bancos de dados não relacional, nesse caso, sendo o Firebase. </p>
  
## 🔧 Tecnologias
<p> As principais tecnologias utilizadas no projeto foram : </p>
  <div>
    <ul>
      <li>Java com Android Studio</li>
      <li>Firebase Authentication</li>
      <li>Firebase RealtimeDatabase</li>
      <li>Firebase Storage</li>        
     </ul>   
 </div>
 
 ## 💡 Funcionalidades
 
 <div>
    <ul>
      <li>Cadastro e Login de usuários (Autenticação via Email)</li>
      <li>Configurar foto e nome do usuário (foto obtida através da câmera e galeria)</li>
      <li>Enviar mensagens de texto e imagens para os contatos</li>
      <li>Criação de grupos, onde pode-se configurar o nome e foto do grupo, podendo mandar mensagens de texto e imagens</li>    
      <li>Aba para as últimas mensagens enviadas por grupos ou contatos do usuário</li>
      <li>Filtrar os contatos e as últimas conversas pelo nome através de uma barra de pesquisa</li>
     </ul>   
 </div>
 
 ## 🧪 Como testar
 
 Possuindo o Android Studio instalado, após isso, é preciso configurar o banco de dados, no caso sendo o Firebase, seguindo os passos abaixo :
  <div>
    <ul>
      <li>Criar conta no Firebase, caso não possua</li>
      <li>Criar o projeto no Firebase e linkar com o AndroidStudio, com a chave SHA-1 (gradle/app//tasks/andoird/signingReport) e o pacote base do projeto (AndroidManifests.xml) </li>
      <li>Configurar o Firebase Authentication, habilitando apenas o email</li>
      <li>Configurando o Firebase RealtimeDatabase, com as regras em que qualquer pessoa esteja liberado a alterar o banco de dados</li>    
      <li>Configurar o Firebase Storage</li>
      <li>Colocar o google-services.json na pasta indicada pelo Firebase</li>
      <li>Compilar o projeto e executar via emulador ou físicamente via smartphone ou tablet com sistema Android superiora 4.1( Jelly Bean)</li>
      <li>Obs: Testatado com MIUI Global 12.0.3</li>
     </ul>   
 </div>
 
