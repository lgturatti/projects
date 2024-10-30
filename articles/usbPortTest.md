Testando uma porta USB do computador

O que fazer para solucionar problemas de uma porta USB que não funciona no seu computador?

1. Teste com um dispositivo simples:
Tente conectar um dispositivo USB básico como um mouse e verifique se está funcionando.

2. Limpeza. Com um palito de dentes de madeira e um pequeno pedaço de algodão, faça um "cotonete" com tamanho reduzido o suficiente para entrar na ranhura onde estão os contatos de cabo/porta USB; Aplique no algodão alcool isopropilico ou WD-40. Caso haja algum sinal de corrosão, aparecerá facilmente no algodão (alaranjado ou laranja/marrom), ou ainda, se não houver corrosão é possível a formação do zinabre (verde) caso o equipamento esteja exposto a um local com muita humidade. Se for só poeira, saira marrom/avermelhado ou cinza. Depois de limpa a conexão, tente a primeira opção novamente.

3. Verifique o Gerenciador de dispositivos:
3.1. Windows: pressione a tecla Windows + X e selecione Gerenciador de dispositivos. Expanda a seção “Controladores Universal Serial Bus” e procure por quaisquer dispositivos com um ponto de exclamação amarelo ou um X vermelho. Clique com o botão direito do mouse no dispositivo e selecione “Desinstalar” para removê-lo. Reinicie o computador e o Windows reinstalará os drivers.
3.2. macOS: Vá para Informações do sistema (menu Apple > Sobre este Mac > Relatório do sistema) e selecione “USB” em “Hardware”. Procure por quaisquer dispositivos com um símbolo de aviso ou um status “Não pronto”.
3.3. Linux: use o "lsusb" no terminal antes de conectar algo; e novamente depois de conectar algum dispositivo USB na porta do equipamento. Se aparecer na lista, a porta está OK. Se não montar automaticamente para uso pode ser problema no dispositivo conectado.

3. Use um plugue de loopback:
Um plugue de loopback é um dispositivo USB especializado que testa a funcionalidade da porta. Você pode comprar um (USD 6.00) ou tentar fazer o seu se souber um pouco de eletrônica. O material necessário é um conector USB, um pedaço de fio, um led e um resistor, ferro de solda e calma. O diagrama da pinagem da porta USB está disponível para consulta na Internet.

