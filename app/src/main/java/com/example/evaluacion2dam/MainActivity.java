package com.example.evaluacion2dam;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evaluacion2dam.database.AppDatabase;
import com.example.evaluacion2dam.model.Cliente;
import com.example.evaluacion2dam.model.Servicio;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fabAdd;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private View layoutDashboard;
    private EditText etBuscar;
    
    private TextView tvCountClientes, tvCountPendientes, tvIngresosTotales;

    private AppDatabase db;
    private List<Object> dataList = new ArrayList<>();
    private GenericAdapter adapter;
    private String currentTab = "inicio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        fabAdd = findViewById(R.id.fabAdd);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        layoutDashboard = findViewById(R.id.layoutDashboard);
        etBuscar = findViewById(R.id.etBuscar);

        // Dashboard widgets
        tvCountClientes = findViewById(R.id.tvCountClientes);
        tvCountPendientes = findViewById(R.id.tvCountPendientes);
        tvIngresosTotales = findViewById(R.id.tvIngresosTotales);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GenericAdapter();
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            etBuscar.setVisibility(View.GONE);
            if (id == R.id.nav_inicio) {
                currentTab = "inicio";
                toolbar.setTitle("Dashboard");
                fabAdd.setVisibility(View.GONE);
                layoutDashboard.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                actualizarDashboard();
                return true;
            } else if (id == R.id.nav_clientes) {
                currentTab = "clientes";
                toolbar.setTitle(""); // Ocultamos título para mostrar el buscador
                etBuscar.setVisibility(View.VISIBLE);
                fabAdd.setVisibility(View.VISIBLE);
                layoutDashboard.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                cargarDatos("clientes");
                return true;
            } else if (id == R.id.nav_servicios) {
                currentTab = "servicios";
                toolbar.setTitle("Servicios");
                fabAdd.setVisibility(View.VISIBLE);
                layoutDashboard.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                cargarDatos("servicios");
                return true;
            }
            return false;
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscarClientes(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> {
            if (currentTab.equals("clientes")) {
                mostrarDialogoNuevoCliente();
            } else if (currentTab.equals("servicios")) {
                mostrarDialogoNuevoServicio();
            }
        });

        actualizarDashboard();
    }

    private void actualizarDashboard() {
        int clientes = db.appDao().contarClientes();
        int pendientes = db.appDao().contarServiciosPendientes();
        double ingresos = db.appDao().obtenerIngresosTotales();

        tvCountClientes.setText(String.valueOf(clientes));
        tvCountPendientes.setText(String.valueOf(pendientes));
        tvIngresosTotales.setText(String.format(Locale.getDefault(), "$%.2f", ingresos));
        
        tvEmpty.setVisibility(View.GONE);
    }

    private void cargarDatos(String tipo) {
        dataList.clear();
        if (tipo.equals("clientes")) {
            dataList.addAll(db.appDao().obtenerTodosLosClientes());
        } else if (tipo.equals("servicios")) {
            dataList.addAll(db.appDao().obtenerTodosLosServicios());
        }
        
        tvEmpty.setVisibility(dataList.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void buscarClientes(String texto) {
        dataList.clear();
        dataList.addAll(db.appDao().buscarClientesPorNombre("%" + texto + "%"));
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(dataList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarDialogoNuevoCliente() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_nuevo_cliente, null);
        EditText etNombre = view.findViewById(R.id.etNombre);
        EditText etTelefono = view.findViewById(R.id.etTelefono);
        EditText etEmail = view.findViewById(R.id.etEmail);

        builder.setView(view)
                .setPositiveButton("Guardar", (dialog, id) -> {
                    String nombre = etNombre.getText().toString();
                    if (nombre.isEmpty()) return;
                    Cliente c = new Cliente();
                    c.nombre = nombre;
                    c.telefono = etTelefono.getText().toString();
                    c.email = etEmail.getText().toString();
                    db.appDao().insertarCliente(c);
                    cargarDatos("clientes");
                })
                .setNegativeButton("Cancelar", null).show();
    }

    private void mostrarDialogoNuevoServicio() {
        List<Cliente> clientes = db.appDao().obtenerTodosLosClientes();
        if (clientes.isEmpty()) {
            Toast.makeText(this, "Registre un cliente primero", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_nuevo_servicio, null);
        Spinner spClientes = view.findViewById(R.id.spinnerClientes);
        Spinner spEstado = view.findViewById(R.id.spinnerEstado);
        EditText etTipo = view.findViewById(R.id.etTipoServicio);
        EditText etMano = view.findViewById(R.id.etManoObra);
        EditText etMate = view.findViewById(R.id.etMateriales);

        spClientes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clientes));

        builder.setView(view)
                .setPositiveButton("Guardar", (dialog, id) -> {
                    Cliente sel = (Cliente) spClientes.getSelectedItem();
                    Servicio s = new Servicio();
                    s.clienteId = sel.id;
                    s.tipoServicio = etTipo.getText().toString();
                    s.estado = spEstado.getSelectedItem().toString();
                    
                    try {
                        s.costoManoObra = Double.parseDouble(etMano.getText().toString());
                        s.costoMateriales = Double.parseDouble(etMate.getText().toString());
                    } catch (Exception e) {
                        s.costoManoObra = 0;
                        s.costoMateriales = 0;
                    }
                    
                    db.appDao().insertarServicio(s);
                    cargarDatos("servicios");
                })
                .setNegativeButton("Cancelar", null).show();
    }

    class GenericAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public int getItemViewType(int position) {
            return (dataList.get(position) instanceof Cliente) ? 0 : 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int res = (viewType == 0) ? R.layout.item_cliente : R.layout.item_servicio;
            View v = LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
            return (viewType == 0) ? new ClienteViewHolder(v) : new ServicioViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object item = dataList.get(position);
            if (holder instanceof ClienteViewHolder) {
                Cliente c = (Cliente) item;
                ((ClienteViewHolder) holder).nombre.setText(c.nombre);
                ((ClienteViewHolder) holder).tel.setText(c.telefono);
                ((ClienteViewHolder) holder).email.setText(c.email);
            } else {
                Servicio s = (Servicio) item;
                ((ServicioViewHolder) holder).tipo.setText(s.tipoServicio);
                ((ServicioViewHolder) holder).estado.setText(s.estado);
                ((ServicioViewHolder) holder).idC.setText("ID Cliente: " + s.clienteId);
                
                holder.itemView.setOnLongClickListener(v -> {
                    if (s.estado.equals("Pendiente")) {
                        db.appDao().eliminarServicio(s);
                        cargarDatos("servicios");
                    } else {
                        Toast.makeText(MainActivity.this, "No se puede borrar servicios en proceso", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            }
        }
        @Override
        public int getItemCount() { return dataList.size(); }
    }

    static class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, tel, email;
        ClienteViewHolder(View v) { super(v); nombre=v.findViewById(R.id.tvNombreCliente); tel=v.findViewById(R.id.tvTelefonoCliente); email=v.findViewById(R.id.tvEmailCliente); }
    }

    static class ServicioViewHolder extends RecyclerView.ViewHolder {
        TextView tipo, estado, idC;
        ServicioViewHolder(View v) { super(v); tipo=v.findViewById(R.id.tvTipoServicio); estado=v.findViewById(R.id.tvEstadoServicio); idC=v.findViewById(R.id.tvClienteId); }
    }
}