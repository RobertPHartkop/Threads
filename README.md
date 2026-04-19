
# Multiplicação de Matrizes
## Descrição do Projeto
Este projeto realiza a análise de desempenho da multiplicação de matrizes quadradas, comparando implementações sequenciais e paralelas em duas linguagens distintas: **C (com OpenMP)** e **Java**. 

O objetivo principal é medir o *Speedup* e a *Eficiência* em um processador **Apple M4** (10 núcleos: 4 de desempenho e 6 de eficiência), observando como o hardware lida com o barramento de memória em diferentes escalas de carga (1.000 a 10.000 elementos).

## Estrutura do Repositório
A organização das pastas é fundamental para o funcionamento dos caminhos relativos utilizados nos códigos.

```plain text
THREADS/
├── .vscode/
├── input/
│   ├── info.txt
│   ├── Matriz(1000)1.bin
│   ├── Matriz(1000)2.bin
│   ├── Matriz(5000)1.bin
│   ├── Matriz(5000)2.bin
│   ├── Matriz(10000)1.bin
│   └── Matriz(10000)2.bin
├── output/
│   ├── c/
│   │   ├── info.txt
│   │   ├── resultado_1000.bin
│   │   ├── resultado_5000.bin
│   │   ├── resultado_10000.bin
│   │   ├── resultados.csv
│   │   ├── speedup_1000.png
│   │   ├── speedup_5000.png
│   │   ├── speedup_10000.png
│   │   └── speedup_combinado.png
│   └── java/
│       └── info.txt
├── ThreadsC/
│   ├── info.txt
│   ├── multiplicador
│   ├── multiplicador_aut
│   ├── multiplicador_aut.c
│   └── multiplicador.c
├── ThreadsJava/
│   └── info.txt
├── .gitignore
└── gerar_graficos.py
````

## Pré-requisitos

- **C:** GCC instalado via Homebrew (ex: `gcc-15`) para suporte ao OpenMP.
- **Java:** JDK 17 ou superior.
- **Python 3:** Bibliotecas `pandas` e `matplotlib` para os gráficos.

## Passo a Passo para Execução

### 1. Geração das Matrizes de Teste

Para que os programas funcionem, você precisa dos arquivos binários na pasta `input`. Use o script abaixo (`gerar_matrizes.py`):

Python

```
import numpy as np
import os

def criar_binario(n):
    for i in [1, 2]:
        caminho = f"input/Matriz({n}){i}.bin"
        # Gera matriz NxN de inteiros (int32)
        dados = np.random.randint(0, 10, size=(n, n), dtype=np.int32)
        with open(caminho, "wb") as f:
            f.write(np.int32(n).tobytes()) # Header N linhas
            f.write(np.int32(n).tobytes()) # Header N colunas
            f.write(dados.tobytes())
        print(f"Criado: {caminho}")

for tam in [1000, 2000, 5000, 10000]:
    criar_binario(tam)
```

* Execute com: `python3 gerar_matrizes.py`
* Coloque as matrizes geradas com os nomes conforme ilustrado na arvore do projeto exemplo acima

### 2. Compilação e Execução (C)

Os comandos devem ser executados de dentro da pasta `c/` para que os caminhos relativos `../input` e `../output` funcionem corretamente.

**Compilar:**

Bash

```
cd c
/opt/homebrew/bin/gcc-15 -O3 -fopenmp main_lote.c -o multiplicador
```

**Executar:**

Bash

```
./multiplicador
```

_Os resultados serão salvos em `../output/c/resultados.csv`._

### 3. Geração dos Gráficos

Com os arquivos `resultados.csv` presentes nas pastas de saída, execute o script na raiz do projeto:

Bash

```
python3 gerar_graficos.py
```

O script salvará automaticamente as imagens `.png` (individuais e combinadas) dentro de `output/c/` e `output/java/`.

## Observações sobre o Git

Este projeto utiliza um arquivo `.gitignore` rigoroso para evitar o envio de arquivos pesados ao GitHub. Estão sendo ignorados:

- Todos os arquivos `.bin` (Matrizes de entrada e saída).
- Todos os arquivos `.csv` (Métricas de tempo).
- Todos os arquivos `.png` (Gráficos).
- Executáveis e arquivos `.class`.

