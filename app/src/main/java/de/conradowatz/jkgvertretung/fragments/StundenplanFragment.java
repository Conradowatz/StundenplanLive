package de.conradowatz.jkgvertretung.fragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

import de.conradowatz.jkgvertretung.MyApplication;
import de.conradowatz.jkgvertretung.R;
import de.conradowatz.jkgvertretung.adapters.StundenplanPagerAdapter;
import de.conradowatz.jkgvertretung.tools.VertretungsData;
import de.conradowatz.jkgvertretung.variables.DayUpdatedEvent;
import de.conradowatz.jkgvertretung.variables.Klasse;
import de.greenrobot.event.EventBus;

public class StundenplanFragment extends Fragment {

    public static final int MODE_STUNDENPLAN = 1;
    public static final int MODE_VERTRETUNGSPLAN = 2;
    public static final int MODE_ALGVERTRETUNGSPLAN = 3;
    public static final int MODE_KLASSENPLAN = 4;

    private View contentView;

    private ViewPager viewPager;
    private TabLayout tabs;
    private Spinner spinner;

    private int mode;
    private Integer klassenIndex;

    private EventBus eventBus = EventBus.getDefault();

    public StundenplanFragment() {
        // Required empty public constructor
    }

    public static StundenplanFragment newInstance(int mode) {

        Bundle args = new Bundle();
        args.putInt("mode", mode);

        StundenplanFragment fragment = new StundenplanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        eventBus.register(this);

        if (savedInstanceState == null) {

            Bundle arguments = getArguments();
            mode = arguments.getInt("mode");
        } else {

            mode = savedInstanceState.getInt("mode");
            klassenIndex = savedInstanceState.getInt("klassenIndex");
        }

        //Analytics
        MyApplication analytics = (MyApplication) getActivity().getApplication();
        switch (mode) {
            case MODE_STUNDENPLAN:
                analytics.fireScreenHit("Stundenplan");
                break;
            case MODE_VERTRETUNGSPLAN:
                analytics.fireScreenHit("Vertretungsplan");
                break;
            case MODE_ALGVERTRETUNGSPLAN:
                analytics.fireScreenHit("Allgemeiner Vertretungsplan");
                break;
            case MODE_KLASSENPLAN:
                analytics.fireScreenHit("Klassenplan");
        }

        if (mode == MODE_KLASSENPLAN)
            contentView = inflater.inflate(R.layout.fragment_klassenplan, container, false);
        else contentView = inflater.inflate(R.layout.fragment_stundenplan, container, false);
        viewPager = (ViewPager) contentView.findViewById(R.id.viewPager);
        tabs = (TabLayout) contentView.findViewById(R.id.materialTabs);

        if (mode == MODE_KLASSENPLAN) {

            spinner = (Spinner) contentView.findViewById(R.id.spinner);
            setUpSpinner();
            if (klassenIndex != null) setUpViewPager(klassenIndex);
        } else {
            setUpViewPager(null);
        }

        return contentView;
    }

    private void setUpSpinner() {

        ArrayList<String> klassennamenListe = new ArrayList<>();
        for (Klasse klasse : VertretungsData.getsInstance().getKlassenList()) {
            klassennamenListe.add(klasse.getName());
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.klassenplan_spinner_text, klassennamenListe);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (klassenIndex == null || position != klassenIndex) {
                    Log.d("JKGDEBUG", "Ich mach doch schon!");
                    setUpViewPager(position);
                    klassenIndex = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setUpViewPager(Integer klassenIndex) {

        StundenplanPagerAdapter adapter = new StundenplanPagerAdapter(getChildFragmentManager(), mode, klassenIndex);
        viewPager.setAdapter(adapter);

        tabs.setTabTextColors(ContextCompat.getColor(getContext(), R.color.white), ContextCompat.getColor(getContext(), R.color.white));
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setupWithViewPager(viewPager);
        tabs.setScrollPosition(0, 0, true);
    }

    /**
     * Läd den ViewPager neu, wenn Tage hinzugefügt wurden
     */
    public void onEvent(DayUpdatedEvent event) {

        if (viewPager == null) return;

        if (event.getPosition() > viewPager.getAdapter().getCount()) {

            viewPager.getAdapter().notifyDataSetChanged();
            tabs.setupWithViewPager(viewPager);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt("mode", mode);
        if (klassenIndex != null) outState.putInt("klassenIndex", klassenIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        eventBus.unregister(this);
        super.onStop();
    }
}
