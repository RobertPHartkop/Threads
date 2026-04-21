#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <omp.h>

typedef struct
{
    int n;
    int *dados;
} Matriz;

Matriz ler_matriz(const char *caminho)
{
    Matriz mat = {0, NULL};
    FILE *f = fopen(caminho, "rb");
    if (!f)
        return mat;

    int header[2];
    fread(header, sizeof(int), 2, f);
    mat.n = header[0];

    int total = mat.n * mat.n;
    mat.dados = (int *)malloc(total * sizeof(int));
    fread(mat.dados, sizeof(int), total, f);
    fclose(f);
    return mat;
}

void salvar_matriz_binario(const char *caminho, int n, long long *dados)
{
    FILE *f = fopen(caminho, "wb");
    if (!f)
    {
        printf("  [ERRO] Nao foi possivel exportar. A pasta '../output' existe?\n");
        return;
    }

    int header[2] = {n, n};
    fwrite(header, sizeof(int), 2, f);

    // Escreve os dados (long long = 8 bytes)
    fwrite(dados, sizeof(long long), n * n, f);

    fclose(f);
    printf("  Matriz resultante exportada para: %s\n", caminho);
}

void salvar_metricas(int tamanho, int threads, double tempo)
{
    FILE *f = fopen("../output/c/resultados_manual.csv", "a");
    if (!f)
    {
        printf("  [ERRO] Nao foi possivel salvar metricas. A pasta 'output' existe?\n");
        return;
    }
    fseek(f, 0, SEEK_END);
    if (ftell(f) == 0)
        fprintf(f, "tamanho_matriz,threads,tempo_segundos\n");
    fprintf(f, "%d,%d,%.6f\n", tamanho, threads, tempo);
    fclose(f);
}

void executar_sequencial(Matriz A, Matriz B)
{
    int n = A.n;
    long long *C = (long long *)malloc(n * n * sizeof(long long));

    printf("  Iniciando multiplicacao sequencial %dx%d...\n", n, n);
    double inicio = omp_get_wtime();

    for (int i = 0; i < n; i++)
    {
        for (int j = 0; j < n; j++)
        {
            long long soma = 0;
            for (int k = 0; k < n; k++)
            {
                soma += (long long)A.dados[i * n + k] * B.dados[k * n + j];
            }
            C[i * n + j] = soma;
        }
    }

    double tempo = omp_get_wtime() - inicio;
    printf("  [Seq] Concluido em %.6f segundos.\n", tempo);

    salvar_metricas(n, 1, tempo);

    char nome_arquivo[256];
    sprintf(nome_arquivo, "../output/c/resultado_%d_manual.bin", n);
    salvar_matriz_binario(nome_arquivo, n, C);

    free(C);
}

void executar_paralela(Matriz A, Matriz B, int num_threads)
{
    int n = A.n;
    long long *C = (long long *)malloc(n * n * sizeof(long long));

    printf("  Iniciando multiplicacao paralela %dx%d com %d threads...\n", n, n, num_threads);

    omp_set_num_threads(num_threads);

    double inicio = omp_get_wtime();

#pragma omp parallel for
    for (int i = 0; i < n; i++)
    {
        for (int j = 0; j < n; j++)
        {
            long long soma = 0;
            for (int k = 0; k < n; k++)
            {
                soma += (long long)A.dados[i * n + k] * B.dados[k * n + j];
            }
            C[i * n + j] = soma;
        }
    }

    double tempo = omp_get_wtime() - inicio;
    printf("  [Par - %02d threads] Concluido em %.6f segundos.\n", num_threads, tempo);

    salvar_metricas(n, num_threads, tempo);

    char nome_arquivo[256];
    sprintf(nome_arquivo, "../output/c/resultado_%d_manual.bin", n);
    salvar_matriz_binario(nome_arquivo, n, C);

    free(C);
}

int main()
{
    int opcao;
    int dimensao;
    char caminhoA[256], caminhoB[256];

    while (1)
    {
        printf("\n=== Menu Interativo: Multiplicacao de Matrizes ===\n");
        printf("1. Multiplicacao Sequencial\n");
        printf("2. Multiplicacao Paralela\n");
        printf("0. Sair\n");
        printf("Escolha: ");

        if (scanf("%d", &opcao) != 1)
        {
            while (getchar() != '\n')
                ; // Limpa buffer
            continue;
        }

        if (opcao == 0)
            break;

        if (opcao == 1 || opcao == 2)
        {
            printf("Digite a dimensao da matriz (ex: 1000, 5000, 10000): ");
            if (scanf("%d", &dimensao) != 1)
            {
                while (getchar() != '\n')
                    ;
                printf("  [ERRO] Entrada invalida.\n");
                continue;
            }

            sprintf(caminhoA, "../input/Matriz(%d)1.bin", dimensao);
            sprintf(caminhoB, "../input/Matriz(%d)2.bin", dimensao);

            printf("-> Buscando matrizes na pasta input\n");

            Matriz A = ler_matriz(caminhoA);
            Matriz B = ler_matriz(caminhoB);

            if (A.dados && B.dados && A.n == B.n)
            {
                if (opcao == 1)
                {
                    executar_sequencial(A, B);
                }
                else if (opcao == 2)
                {
                    int num_threads;
                    printf("Numero de threads: ");
                    scanf("%d", &num_threads);
                    executar_paralela(A, B, num_threads);
                }
            }
            else
            {
                printf("  [ERRO] Falha ao carregar matrizes de %dx%d.\n", dimensao, dimensao);
                printf("  Verifique se os arquivos %s e %s existem!\n", caminhoA, caminhoB);
            }

            if (A.dados)
                free(A.dados);
            if (B.dados)
                free(B.dados);
        }
        else
        {
            printf("  [ERRO] Opcao invalida.\n");
        }
    }

    printf("Saindo...\n");
    printf("Os dados da execucao foram salvos em ../output/c/resultados_manual.csv\n");
    return 0;
}