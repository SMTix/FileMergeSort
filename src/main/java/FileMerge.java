import java.io.*;
import java.util.Arrays;

public class FileMerge {

    //Метод отвечает за проверку входных данных (ключей, файлов и т.п.) и запуск основного алгоритма
    public static void mergeStart(String ... args) {

        String[] filesInput; //Отдельный массив для входных файлов

        //Проверка на кол-во параметров
        if (args.length == 0) {
            System.out.println("Пустая командная строка");
            return;
        }

        if (args[0].matches("-[ad]")) { //Проверяем ключ: '-a' или '-d'

            if (args[1].matches("-[is]")) { //Проверяем ключ: -i или -s
                filesInput = Arrays.copyOfRange(args, 3, args.length);
                merge(args[0].charAt(1), args[1].charAt(1), args[2], filesInput);
            } else {
                System.out.println("Неправильный ключ типа файла (int/String)\n");
            }

        } else if (args[1].matches("[^-].*")) { //Если ключа нет (необязательный ключ), то ставим ключ '-a' по умолчанию
                filesInput = Arrays.copyOfRange(args, 2, args.length);
                merge('a', args[0].charAt(1), args[1], filesInput);
        } else {
            System.out.println("Неправильный ключ порядка сортировки\n");
        }
    }

    /*
    Метод, где происходит слияние файлов:
        key1 - ключи '-a', '-d';
        key2 - ключи '-i', '-s';
        fileOutput - выходной файл
        filesInput - массив входных файлов
    */
    private static void merge(char key1, char key2, String fileOutput, String ... filesInput) {

        int filesNum = filesInput.length; //Кол-во входных файлов
        BufferedReader[] fileReader = null; //Массив входных файлов
        File directory = new File("");

        //Указываем выходной файл
        try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(directory.getAbsolutePath() + "/" + fileOutput))) {

            //Открываем все входные файлы для чтения
            fileReader = new BufferedReader[filesNum];
            for (int i = 0; i < filesNum; i++) {
                fileReader[i] = new BufferedReader(new FileReader(directory.getAbsolutePath() + "/" + filesInput[i]));
            }

            //Записываем в массив по первому символу из каждого входного файла
            String[] symbols = new String[filesNum];
            for (int i = 0; i < filesNum; i++) {
                symbols[i] = fileReader[i].readLine(); //Считываем строку
                if (symbols[i] != null && key2 == 'i') { //Для типа integer: проверяем тип данных, пока не найдем нужный
                    try {
                        Integer.parseInt(symbols[i]);
                    } catch (NumberFormatException exc) {
                        i--; //В случае ошибки - вновь считываем из того же потока
                    }
                }
            }

            String min, lastMin = null; //min - используется для минимальной строки, lastMin - используется для проверки порядка сортировки в файлах
            int index = -1; //Индекс файла, откуда взята минимальная строка

            //Вторая фаза merge sort - слияние файлов в один
            while (true) {
                min = null;

                //Находим первый доступный элемент, указываем его в качестве минимального
                for (int i = 0; i < filesNum; i++) {
                    if (symbols[i] != null) {
                        min = symbols[i];
                        index = i;
                        break;
                    }
                }

                //Проверяем min: если все входные файлы подошли к концу, то min должно быть равно null, выходим из цикла
                if (min == null) {
                    break;
                }

                //Цикл находит минимальный элемент в массиве строк
                for (int i = 0; i < filesNum; i++) {
                    try { //Блок try-catch позволяет отловить тот случай, когда в файле с int данными встречается какой-либо другой тип
                        if (symbols[i] != null && comparing(key1, key2, min, symbols[i])) {
                            min = symbols[i];
                            index = i;
                        }
                    } catch (NumberFormatException exc) {
                        symbols[i] = fileReader[i--].readLine(); //При ошибке - пропускаем строку и переходим к следующей, уменьшив i на 1
                    }
                }

                //Если выбранная строка не соответсвует порядку сортировки (сравниваем с lastMin) - пропускаем её, переходим к следующей
                try {
                    if (lastMin != null && comparing(key1, key2, lastMin, min)) {
                        symbols[index] = fileReader[index].readLine();
                        continue;
                    }
                } catch (NumberFormatException exc) { //Если min содержит неправильный формат (для int), то также пропускаем строку
                    symbols[index] = fileReader[index].readLine();
                    continue;
                }

                //Если выбранная строка (ключ -s) содержит пробелы, то она - ошибочная, её необходимо убрать
                if (key2 == 's' && min.matches(".*\\s.*")) {
                    symbols[index] = fileReader[index].readLine();
                    continue;
                }

                fileWriter.write(min); //Записываем минимальную строку в выходной файл
                fileWriter.write('\n');
                lastMin = min;
                symbols[index] = fileReader[index].readLine(); //Считываем следующую строку из файла
            }

        } catch (ArrayIndexOutOfBoundsException exc) {
            System.out.println("Выход за границы массива:\n" + exc.toString() + "\n");
        } catch (IOException exc) {
            System.out.println("Ошибка ввода-вывода:\n" + exc.toString() + "\n");
        } finally {

            //Закрываем потоки данных
            try {
                for (int i = 0; i < filesNum; i++) {
                    if (fileReader[i] != null) {
                        fileReader[i].close();
                    }
                }
            } catch (NullPointerException exc) {
                System.out.println("Попытка закрыть объект null:\n" + exc.toString() + "\n");
            } catch (ArrayIndexOutOfBoundsException exc) {
                System.out.println("Выход за границы массива:\n" + exc.toString() + "\n");
            } catch (IOException exc) {
                System.out.println("Ошибка ввода-вывода:\n" + exc.toString() + "");
            }
        }
    }

    //Метод для сравнения двух значений с использованием определённых ключей
    private static boolean comparing(char key1, char key2, String s1, String s2) throws NumberFormatException {

        switch (key2) {
            case 'i': //Для int - преобразовываем строку в Integer
                switch (key1) {
                    case 'a':
                        return Integer.parseInt(s1) > Integer.parseInt(s2);
                    case 'd':
                        return Integer.parseInt(s1) < Integer.parseInt(s2);
                    default:
                        return false;
                }

            case 's': //Для String - сравниваем через compareTo()
                switch (key1) {
                    case 'a':
                        return s1.compareTo(s2) > 0;
                    case 'd':
                        return s1.compareTo(s2) < 0;
                    default:
                        return false;
                }

            default:
                return false;
        }
    }
}