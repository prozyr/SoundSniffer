package com.example.soundsniffier;
import com.github.mikephil.charting.data.Entry;
import java.util.ArrayList;
import java.util.List;
public class GlobalData {

    private static final GlobalData instance = new GlobalData();
    private List<Entry> entries2 = new ArrayList<>();

    private GlobalData() {
        // Prywatny konstruktor, aby zapobiec bezpośredniemu tworzeniu instancji
    }

    public static GlobalData getInstance() {
        return instance;
    }

    public List<Entry> getEntries2() {
        return entries2;
    }
}
