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
        printf("Erro na exportacao do arquivo %s. A pasta 'output' existe na raiz do projeto?\n", caminho);
        return;
    }
    int header[2] = {n, n};
    fwrite(header, sizeof(int), 2, f);
    fwrite(dados, sizeof(long long), n * n, f);
    fclose(f);
}

void salvar_metricas(int tamanho, int threads, double tempo)
{
    FILE *f = fopen("../output/c/resultados.csv", "a");
    if (!f)
    {
        printf("Erro ao salvar metricas. A pasta 'output' existe na raiz?\n");
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
    printf("    [Seq] Concluido em %.6f segundos.\n", tempo);
    salvar_metricas(n, 1, tempo);

    char nome_arquivo[256];
    sprintf(nome_arquivo, "../output/c/resultado_%d.bin", n);
    salvar_matriz_binario(nome_arquivo, n, C);
    free(C);
}

void executar_paralela(Matriz A, Matriz B, int num_threads)
{
    int n = A.n;
    long long *C = (long long *)malloc(n * n * sizeof(long long));
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
    printf("    [Par - %02d threads] Concluido em %.6f segundos.\n", num_threads, tempo);
    salvar_metricas(n, num_threads, tempo);

    char nome_arquivo[256];
    sprintf(nome_arquivo, "../output/c/resultado_%d.bin", n);
    salvar_matriz_binario(nome_arquivo, n, C);
    free(C);
}

int main()
{
    int tamanhos[] = {1000, 5000, 10000};
    int num_tamanhos = sizeof(tamanhos) / sizeof(tamanhos[0]);

    int threads[] = {1, 2, 3, 4, 5, 6, 8, 10};
    int num_threads_testes = sizeof(threads) / sizeof(threads[0]);

    int repeticoes = 3;

    printf("=== Iniciando Bateria Completa de Testes Automatizados ===\n\n");

    for (int t_idx = 0; t_idx < num_tamanhos; t_idx++)
    {
        int n = tamanhos[t_idx];
        char caminhoA[256], caminhoB[256];

        // Sobe um nível (../) e busca na pasta input
        sprintf(caminhoA, "../input/Matriz(%d)1.bin", n);
        sprintf(caminhoB, "../input/Matriz(%d)2.bin", n);

        printf("-> Carregando matrizes %dx%d\n", n, n);
        Matriz A = ler_matriz(caminhoA);
        Matriz B = ler_matriz(caminhoB);

        if (!A.dados || !B.dados || A.n != B.n)
        {
            printf("  [ERRO] Falha ao carregar %s ou %s.\n\n", caminhoA, caminhoB);
            if (A.dados)
                free(A.dados);
            if (B.dados)
                free(B.dados);
            continue;
        }

        for (int th_idx = 0; th_idx < num_threads_testes; th_idx++)
        {
            int num_threads = threads[th_idx];

            for (int r = 1; r <= repeticoes; r++)
            {
                printf("  Testando: Matriz %dx%d | Threads: %02d | Repeticao: %d/%d...\n",
                       n, n, num_threads, r, repeticoes);

                if (num_threads == 1)
                {
                    executar_sequencial(A, B);
                }
                else
                {
                    executar_paralela(A, B, num_threads);
                }
            }
        }

        printf("\nConcluido testes para dimensao %d.\n", n);
        printf("--------------------------------------------------\n\n");

        free(A.dados);
        free(B.dados);
    }

    printf("=== Bateria de testes concluida! ===\n");
    printf("Os dados estao em ../output/c/resultados.csv\n");

    return 0;
}