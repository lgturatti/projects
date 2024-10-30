# Acessando o github no prompt com segurança


:warning: ATENÇÃO: Estas anotações foram feitas utilizando Linux (derivado do Debian)

Para instalação em Windows, recomendo utilizar o [chocolatey](https://chocolatey.org)

## Indice

- [Acessando o github no prompt com segurança](#acessando-o-github-no-prompt-com-segurança)
  - [Indice](#indice)
  - [Verifique se há chaves SSH em seu computador](#verifique-se-há-chaves-ssh-em-seu-computador)
  - [Caso não haja um par de arquivos `chave` e `chave.pub`](#caso-não-haja-um-par-de-arquivos-chave-e-chavepub)
  - [Adicionar sua chave SSH ao ssh-agent (no seu computador)](#adicionar-sua-chave-ssh-ao-ssh-agent-no-seu-computador)
  - [Adicione sua chave SSH a sua conta no github (opcional)](#adicione-sua-chave-ssh-a-sua-conta-no-github-opcional)
  - [Instalando o GH](#instalando-o-gh)
  - [Configurando o GH](#configurando-o-gh)
  - [Outras Referências](#outras-referências)


## Verifique se há chaves SSH em seu computador
[^ Início](#acessando-o-github-no-prompt-com-segurança)

Veja se o diretório ssh existe e se possui arquivos.

```
$ ls -al ~/.ssh
```

Se houver, o padrão de resposta terá algo como:

- id_rsa.pub
- id_ecdsa.pub
- id_ed25519.pub

Existindo chaves geradas, siga para o [item 3](#adicionar-sua-chave-ssh-ao-ssh-agent-no-seu-computador)

## Caso não haja um par de arquivos `chave` e `chave.pub`
[^ Início](#acessando-o-github-no-prompt-com-segurança)

Crie um par privado/público de chaves para utilizar

```
$ ssh-keygen -t ed25519 -C "seu_email@provedor.com" 
```

caso este comando não funcione, tente:

```
$ ssh-keygen -t rsa -b 4096 -C "seu_email@provedor.com" 
```

Isto cria uma nova chave SSH, usando o nome de e-mail fornecido como uma etiqueta.

```
> Generating public/private ALGORITHM key pair. 
```

Quando for solicitado a inserir um arquivo para salvar a chave, pressione Enter para aceitar o local padrão do arquivo. Observe que, se você criou chaves SSH anteriormente, ssh-keygen pode pedir que você reescreva outra chave. Nesse caso, recomendamos criar uma chave SSH personalizada. Para fazer isso, digite o local do arquivo padrão e substitua id_ALGORITHM pelo nome da chave personalizada.

```
> Enter a file in which to save the key (/home/YOU/.ssh/id_ALGORITHM): [Press enter] 
```

No prompt, digite uma frase secreta segura. Ex: BatatinhaQuandoNasceEspalhaRama

```
> Enter passphrase (empty for no passphrase): [Type a passphrase]

> Enter same passphrase again: [Type passphrase again]
```

## Adicionar sua chave SSH ao ssh-agent (no seu computador)
[^ Início](#acessando-o-github-no-prompt-com-segurança)

Inicie o ssh-agent em segundo plano:

```
$ eval "$(ssh-agent -s)"
> Agent pid 59566
```
Adicione sua chave SSH privada ao ssh-agent
```
ssh-add ~/.ssh/id_ed25519
```

## Adicione sua chave SSH a sua conta no github (opcional)
[^ Início](#acessando-o-github-no-prompt-com-segurança)

Faça o login no [github](https://github.com)

Copie a chave pública SSH para a sua área de transferência.
```
$ cat ~/.ssh/id_ed25519.pub
ssh-ed25519 AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
```
No canto superior direito de qualquer página do GitHub, clique sua foto de perfil e, em seguida, clique em `Configurações/Settings`.

No menu existente no lado esquerdo, na seção "Acesso", clique em `Chaves SSH e GPG`.

Na seção `Chaves SSH`, clique no botão `Nova chave SSH`

No campo `Título`, adicione uma descrição como `Laptop pessoal`

No campo `Tipo de chave` escolha `Chave de autenticação`

No campo `Chave`, cole a chave copiada para a área de transferência (ssh-ed25519 ...)

Clique no botão `Adicionar chave SSH`.

Pronto!

##  Instalando o GH
[^ Início](#acessando-o-github-no-prompt-com-segurança)

Faça a instalação da chave e assinatura; atualize a lista de pacotes e instale o gh.

```
$ curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo gpg --dearmor -o /usr/share/keyrings/githubcli-archive-keyring.gpg

$ echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null

$ sudo apt update

$ sudo apt install gh
```

## Configurando o GH
[^ Início](#acessando-o-github-no-prompt-com-segurança)

Faça o vínculo de seu computador com sua conta do github.

```
$ gh auth login

? What account do you want to log into? GitHub.com
? What is your preferred protocol for Git operations on this host? SSH
? Upload your SSH public key to your GitHub account? 
  Use SKIP se fez o processo do item 4
  -ou-
  Permita o uso da chave indicada
? How would you like to authenticate GitHub CLI? Login with a web browser

! First copy your one-time code: ABCD-1234
Press Enter to open github.com in your browser...

No navegador o código ABCD-1234 será solicitado

✓ Authentication complete.
- gh config set -h github.com git_protocol ssh
✓ Configured git protocol
✓ Logged in as SEU_USUARIO
! You were already logged in to this account
```

5.3. Selecionando um editor preferencial

Por padrão, no Linux e Mac o editor será o `nano`, enquanto no Windows, o padrão é o `bloco de notas`.

```
$ gh config set editor nome_do_editor
```

## Outras Referências
[^ Início](#acessando-o-github-no-prompt-com-segurança)

[Verifique se há chaves SSH em seu computador](https://docs.github.com/pt/authentication/connecting-to-github-with-ssh/checking-for-existing-ssh-keys)

[Gerando uma nova chave SSH e adicionando-a ao agente SSH](https://docs.github.com/pt/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent)

[GitHub CLI Manual](https://cli.github.com/manual/)

[Instalando o GitHub CLI (gh)](https://github.com/cli/cli#installation)

[Instalando o GH no Linux](https://github.com/cli/cli/blob/trunk/docs/install_linux.md)

[GH gist create](https://cli.github.com/manual/gh_gist_create)



