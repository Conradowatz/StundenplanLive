package de.conradowatz.jkgvertretung.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DecimalFormat;

import de.conradowatz.jkgvertretung.R;
import de.conradowatz.jkgvertretung.activities.FachActivity;
import de.conradowatz.jkgvertretung.activities.ManagerActivity;
import de.conradowatz.jkgvertretung.adapters.NotenUebersichtRecyclerAdpater;
import de.conradowatz.jkgvertretung.events.AnalyticsScreenHitEvent;
import de.conradowatz.jkgvertretung.events.KursChangedEvent;
import de.conradowatz.jkgvertretung.events.NotenChangedEvent;
import de.conradowatz.jkgvertretung.tools.LocalData;
import de.conradowatz.jkgvertretung.variables.Fach;

public class NotenUebersichtFragment extends Fragment implements NotenUebersichtRecyclerAdpater.Callback {

    private View contentView;
    private RecyclerView recyclerView;
    private TextView durchschnittText;
    private FloatingActionButton fab;

    private EventBus eventBus = EventBus.getDefault();

    public NotenUebersichtFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Analytics
        eventBus.post(new AnalyticsScreenHitEvent("Notenübersicht"));

        contentView = inflater.inflate(R.layout.fragment_notenuebersicht, container, false);

        recyclerView = (RecyclerView) contentView.findViewById(R.id.recyclerView);
        durchschnittText = (TextView) contentView.findViewById(R.id.durchschnittText);
        fab = (FloatingActionButton) contentView.findViewById(R.id.fab);

        eventBus.register(this);
        setUp();


        return contentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        eventBus.unregister(this);
    }

    @Subscribe
    public void onEvent(NotenChangedEvent event) {
        setUp();
    }

    @Subscribe
    public void onEvent(KursChangedEvent event) {
        setUp();
    }

    private void setUp() {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        NotenUebersichtRecyclerAdpater adpater = new NotenUebersichtRecyclerAdpater(this, getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adpater);

        double gesamtAverage = 0;
        int n = 0;
        for (Fach f : LocalData.getInstance().getFächer()) {
            Double average = f.getNotenAverage();
            if (average != null) {
                gesamtAverage += average;
                n++;
            }
        }
        if (n > 0) gesamtAverage /= n;
        durchschnittText.setText(n > 0 ? new DecimalFormat("#0.00").format(gesamtAverage) : "n.A.");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startManagerActivity();
            }
        });
    }

    private void startFachActivity(int fachIndex) {

        Intent openFachIntent = new Intent(getContext(), FachActivity.class);
        openFachIntent.putExtra("fachIndex", fachIndex);
        openFachIntent.putExtra("tab", FachActivity.TAB_NOTEN);
        startActivity(openFachIntent);

    }

    private void startManagerActivity() {

        Intent openManagerIntent = new Intent(getContext(), ManagerActivity.class);
        startActivity(openManagerIntent);

    }


    @Override
    public void onFachClicked(int fachIndex) {

        startFachActivity(fachIndex);
    }
}