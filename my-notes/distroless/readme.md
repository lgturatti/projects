# Distroless Docker: Containerizing Apps, not VMs

<br>Executar contêineres como parte de sua infraestrutura de computação é a nova tendência (desde 2017) e segue o que empresas como Twitter, Facebook e Google vêm fazendo há anos. No entanto, as abordagens tradicionais para a construção de contêineres [Docker](https://www.docker.com) são geralmente muito diferentes da abordagem adotada pelos gigantes da tecnologia. Embora seja fácil usar imagens básicas que você encontra no [DockerHub](https://hub.docker.com) para produzir uma imagem de aplicativo, isso leva a imagens maiores (às vezes com 98% de arquivos estranhos!) e introduz componentes não utilizados no seu aplicativo.

<br>A ideia de "distroless containers" vem do objetivo de criar containeres cada vez menores, com menos pacotes desnecessários (visto que o container precisa apenas dos pacotes necessários para executar a aplicação) e por consequência uma menor superfície de possível ataque (aumentando a segurança).

<br>Uma das grandes preocupações com sistemas distribuídos em containeres é a imagem base ou a imagem desses containeres. Isso se deve ao fato dela possuir os pacotes necessários para executar a sua aplicação, pelo menos assim deveria ser. Como o tempo ensinou, esta é uma boa prática estabelecida e seguida pelo próprio Google e outros grandes players de containeres e Kubernetes também.

<br>O [projeto do Google](https://github.com/GoogleContainerTools/distroless) traz algumas imagens já otimizadas para utilização. A [lista de imagens](https://console.cloud.google.com/artifacts/docker/distroless/us/gcr.io?pli=1) pode ser consultada e a navegação entre as opções disponíveis é feita através da navegação no canto inferior direito da tela. Exemplos estão disponíveis [aqui](https://github.com/GoogleContainerTools/distroless/tree/main/examples).

<br>E este [artigo](https://juniorjbn.medium.com/o-que-%C3%A9-esse-tal-de-distroless-d1cc5dcd070e) esclarece melhor os três principais motivos para reduzir o tamanho de um container são: segurança, performance e custo.

<br>Já este outro [artigo](https://pt.linkedin.com/pulse/criando-imagens-de-containers-mais-seguras-e-reyson-barros) traz instruções para iniciar seus testes com um exemplo passo a passo usando Python.

<br>Para criar um novo projeto utilizando Python, você pode consultar a versão mais recente no [site oficial](https://www.python.org/downloads/) e observar a data final de suporte. Esse tipo de informação será utilizado no seu arquivo "Dockerfile".

<br> Confira os playgrounds disponíveis para [python](https://github.com/barbieri/barbieri-playground/tree/master/docker/distroless/python) e [nodejs](https://github.com/barbieri/barbieri-playground/tree/master/docker/distroless/nodejs) elaborados por Barbieri, G.S..

