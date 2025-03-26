# Observando o hardware do computador sem abrir sua máquina

## DMIDECODE
Para obter detalhes do sistema verificado utilizando Linux, é necessário utilizar alguns comandos que precisam de privilégio administrativo (root). Dentre eles, o `dmidecode` permite a obtenção de detalhes do hardware para manter o bom desempenho da sua máquina, principalmente se houver interesse em realizar atualizações do equipamento.

O DMI, ou Desktop Management Information, ou ainda Direct Media Interface, é uma interface de programação que permite que programas coletem informações à respeito do computador. A tabela DMI fica na BIOS e contém informações sobre o hardware, dispostas de maneira padrão.

Algumas pessoas preferem usar o termo "tabelas SMBIOS" (System Management BIOS) ao invés de tabelas DMI.

O comando `biosdecode` decodifica informações do BIOS (Basic Input/Output System).

### Algumas opções do comando
- -h ou −−help : exibe as opções do comando.
- -t tipo : mostra informações apenas do tipo especificado como, por exemplo, bios, processor, memória, cache, etc.
- -V ou −−version : mostra informações sobre o comando.

Esse comando pode ser utilizado indicando um tipo ou palavra chave.

### Utilizando o comando

1. Fornecendo a numeração dos tipos desejados

| Tipo | Informação 
|:-----|:-----------
|0	|BIOS
|1	|System
|2	|Base Board
|3	|Chassis
|4	|Processor
|5	|Memory Controller
|6	|Memory Module
|7	|Cache
|8	|Port Connector
|9	|System Slots
|10	|On Board Devices
|11	|OEM Strings
|12 |System Configuration Options
|13	|BIOS Language
|14	|Group Associations
|15	|System Event Log
|16	|Physical Memory Array
|17	|Memory Device
|18	|32-bit Memory Error
|19	|Memory Array Mapped Address
|20	|Memory Device Mapped Address
|21	|Built-in Pointing Device
|22	|Portable Battery
|23	|System Reset
|24	|Hardware Security
|25	|System Power Controls
|26	|Voltage Probe
|27	|Cooling Device
|28	|Temperature Probe
|29	|Electrical Current Probe
|30	|Out-of-band Remote Access
|31	|Boot Integrity Services
|32	|System Boot
|33	|64-bit Memory Error
|34	|Management Device
|35	|Management Device Component
|36	|Management Device Threshold Data
|37	|Memory Channel
|38	|IPMI Device
|39	|Power Supply
|40	|Additional Information
|41	|Onboard Device
|42 |Management Controller Host Interface

2. Fornecendo palavras-chaves. Abaixo alguns exemplos.


| Palavra-chave | Tipos 
|:--------------|:------
|bios           | 0, 13
|system         | 1, 12, 15, 23, 32
|baseboard      | 2, 10, 41
|chassis        | 3
|processor      | 4
|memory         | 5, 6, 16, 17
|cache          | 7
|connector      | 8
|slot           | 9

### Exemplos

1. Verificar informações sobre a memória

Use: `# dmidecode -t 16`

Use: `# dmidecode -t 17`

2. Para ver as informações das tabelas DMI sobre a memória, pode-se usar

Use: `$ sudo dmidecode -t memory`


No primeiro exemplo, o 16 vai mostrar um resumo sobre o suporte a memória do sistema; se utilizar 17, vai observar quantos bancos de memória o sistema possui e o que tem neles.

Já no segundo exemplo, será mostrado tudo relacionado a memória.

Note que no primeiro exemplo aparece `#` que representa o prompt do administrador enquanto o segundo exemplo mostra `$`, e como o uso do comando requer privilégio administrativo, é usado o comando `sudo` antes do uso do programa e respectivos parâmetros.

## Como listar informações de dispositivos, discos e partições

O comando `lsblk` exibe informações sobre dispositivos de armazenamento. O utilitário costuma ser usado para identificar o nome correto do dispositivo a ser passado para um comando subsequente. O uso mais comum é a consulta para montar/desmontar um dispositivo corretamente caso não seja identificado de forma apropriada pelo sistema.

Também pode ser usado para verificar modelos e detalhes de dispositivos conectados.

Na maioria das vezes, `lsblk` sem qualquer parâmetro adicional é suficiente para ajudar a identificar o disco ou a partição com a qual você deseja trabalhar. No entanto, se você tiver duas ou mais partições do mesmo tamanho, as coisas podem ficar mais confusas. Em outros casos, você pode simplesmente não saber ou se lembrar do tamanho de um disco ou partição específica do seu sistema.

Para obter ajuda sobre quais parâmetros estão disponíveis, use: `lsblk --help`

1. Mostrar sistemas de arquivos armazenados em discos/partições

Use: `# lsblk -o +FSTYPE,LABEL`

2. Mostrar dispositivos removíveis/cartões de memória USB

Use: `# lsblk -o +RM`

3. Mostrar modelo HDD/SSD

Isso é útil quando você deseja procurar o código exato do modelo do seu dispositivo de armazenamento para atualizar seu firmware ou baixar drivers.

Use: `# lsblk -d -o +MODEL`

Se os exemplos aqui não forem suficientes para suas necessidades, consulte as informações de ajuda novamente e combine os parâmetros conforme necessário.

Para fazer isso, basta digitar `lsblk -o +`, seguido pelos nomes das colunas que você deseja gerar. Separe os nomes das colunas com uma vírgula (“,”).

Por exemplo: `# lsblk -o +SCHED,RM,FSTYPE`

## Ferramenta para verificar informação sobre bateria

```
$ upower -e
/org/freedesktop/UPower/devices/line_power_AC0
/org/freedesktop/UPower/devices/battery_BAT0
/org/freedesktop/UPower/devices/DisplayDevice

$ upower -i /org/freedesktop/UPower/devices/battery_BAT0
```

Outras [opções](https://linuxavante.com/3-ferramentas-para-exibir-informacoes-da-bateria-do-laptop-do-linux-da-linha-de-comando)