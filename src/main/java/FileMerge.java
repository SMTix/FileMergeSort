import java.io.*;
import java.util.Arrays;

public class FileMerge {

    //mergeStart отвечает за проверку входных данных (ключей)
    public static void mergeStart(String ... args) {

        String[] filesInput; //Используем отдельный массив с входными файлами

        if (args[0].matches("-[is]")) { //Проверяет ключ -i или -s

            if (args[1].matches("-[ad]")) { //Проверяем ключ -a или -d
                filesInput = Arrays.copyOfRange(args, 2, args.length - 1);
                merge(args[0].charAt(1), args[1].charAt(1), args[args.length - 1], filesInput);

            } else if (args[1].matches("[^-].*")) { //Если второго ключа нет (необязательный ключ), то ставим ключ -a по умолчанию
                filesInput = Arrays.copyOfRange(args, 1, args.length - 1);
                merge(args[0].charAt(1), 'a', args[args.length - 1], filesInput);
            }

        } else {
            System.out.println("Неправильный ключ");
        }
    }

    //Метод, где происходит слияние файлов
    private static void merge(char key1, char key2, String fileOutput, String ... filesInput) {

        int filesNum = filesInput.length; //Кол-во входных файлов
        BufferedReader[] fileReader = null; //Массив входных файлов
        File directory = new File("");

        try(BufferedWriter fileWriter = new BufferedWriter(new FileWriter(directory.getAbsolutePath() + "\\" + fileOutput))) { //Указываем выходной файл

            //Открываем все входные файлы для чтения
            fileReader = new BufferedReader[filesNum];
            for (int i = 0; i < filesNum; i++) {
                fileReader[i] = new BufferedReader(new FileReader(directory.getAbsolutePath() + "\\" + filesInput[i]));
            }

            //Записываем в массив по первому символу из каждого входного файла
            String[] symbols = new String[filesNum];
            for (int i = 0; i < filesNum; i++) {
                symbols[i] = fileReader[i].readLine();
            }

            String min, lastMin = null; //Минимальный элемент из массива символов
            int index = -1; //Индекс файла, откуда взят минимальный элемент

            while (true) { //Вторая фаза merge sort - слияние файлов в один
                min = null;

                //Находим первый доступный элемент, указываем его в качестве минимального
                for (int i = 0; i < filesNum; i++) {
                    if (symbols[i] != null) {
                        min = symbols[i];
                        index = i;
                    }
                }

                //Проверка - есть ли ещё файлы, которые не достигли конца (min должен содержать какую-либо строку)
                if (min == null) {
                    break;
                }

                //Цикл находит минимальный элемент по массиву символов
                for (int i = 0; i < filesNum; i++) {
                    if (symbols[i] != null && comparing(key1, key2, min, symbols[i])) {
                        min = symbols[i];
                        index = i;
                    }
                }

                //Если выбранное значение не соответсвует порядку сортировки - пропускаем элемент, переходим к следующему
                if (lastMin != null && comparing(key1, key2, lastMin, min)) {
                    symbols[index] = fileReader[index].readLine();
                    continue;
                }

                //Если выбранная строка содержит пробелы, то она - ошибочная, её необходимо убрать
                if (key1 == 's' && min.matches(".*\\s.*")) {
                    symbols[index] = fileReader[index].readLine();
                    continue;
                }

                fileWriter.write(min); //Записываем минимальный элемент в выходной файл
                fileWriter.write('\n');
                lastMin = min;
                symbols[index] = fileReader[index].readLine(); //Считываем следующий элемент из файла
            }

        }
        catch (IOException exc) {
            System.out.println(exc.toString());
        }
        finally {

            try {
                for (int i = 0; i < filesNum; i++) {
                    fileReader[i].close();
                }
            }
            catch (NullPointerException exc) {
                System.out.println(exc.toString());
            }
            catch (IOException exc) {
                System.out.println(exc.toString());
            }
        }
    }

    //Метод для сравнения двух значений с использованием определённого ключа
    private static boolean comparing(char key1, char key2, String s1, String s2) {

        switch (key1) {
            case 'i': //Для int
                switch (key2) {
                    case 'a':
                        return Integer.parseInt(s1) > Integer.parseInt(s2);
                    case 'd':
                        return Integer.parseInt(s1) < Integer.parseInt(s2);
                }
            case 's': //Для String
                switch (key2) {
                    case 'a':
                        return s1.compareTo(s2) > 0;
                    case 'd':
                        return s1.compareTo(s2) < 0;
                }
            default:
                return false;
        }
    }
}