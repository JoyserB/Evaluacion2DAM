package com.example.evaluacion2dam;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.evaluacion2dam.model.ServicioConCliente;
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
    private View layoutDashboard, layoutLista, cardBuscar;
    private EditText etBuscar;
    
    private TextView tvCountClientes, tvCountPendientes, tvIngresosTotales;
    private View cardAccesoClientes, cardAccesoServicios;
    private BottomNavigationView bottomNav;

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
        layoutLista = findViewById(R.id.layoutLista);
        cardBuscar = findViewById(R.id.cardBuscar);
        etBuscar = findViewById(R.id.etBuscar);

        tvCountClientes = findViewById(R.id.tvCountClientes);
        tvCountPendientes = findViewById(R.id.tvCountPendientes);
        tvIngresosTotales = findViewById(R.id.tvIngresosTotales);
        
        cardAccesoClientes = findViewById(R.id.cardAccesoClientes);
        cardAccesoServicios = findViewById(R.id.cardAccesoServicios);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GenericAdapter();
        recyclerView.setAdapter(adapter);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                currentTab = "inicio";
                toolbar.setTitle("Dashboard");
                fabAdd.setVisibility(View.GONE);
                layoutDashboard.setVisibility(View.VISIBLE);
                layoutLista.setVisibility(View.GONE);
                actualizarDashboard();
                return true;
            } else if (id == R.id.nav_clientes) {
                currentTab = "clientes";
                toolbar.setTitle("Clientes");
                cardBuscar.setVisibility(View.VISIBLE);
                fabAdd.setVisibility(View.VISIBLE);
                layoutDashboard.setVisibility(View.GONE);
                layoutLista.setVisibility(View.VISIBLE);
                cargarDatos("clientes");
                return true;
            } else if (id == R.id.nav_servicios) {
                currentTab = "servicios";
                toolbar.setTitle("Servicios");
                cardBuscar.setVisibility(View.GONE);
                fabAdd.setVisibility(View.VISIBLE);
                layoutDashboard.setVisibility(View.GONE);
                layoutLista.setVisibility(View.VISIBLE);
                cargarDatos("servicios");
                return true;
            }
            return false;
        });

        cardAccesoClientes.setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_clientes));
        cardAccesoServicios.setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_servicios));

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
                mostrarDialogoNuevoCliente(null);
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
            dataList.addAll(db.appDao().obtenerServiciosConNombre());
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

    private void mostrarDialogoNuevoCliente(Cliente clienteAEditar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_nuevo_cliente, null);
        
        TextView tvTitulo = view.findViewById(R.id.tvTituloDialog);
        EditText etNombre = view.findViewById(R.id.etNombre);
        EditText etTelefono = view.findViewById(R.id.etTelefono);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etDireccion = view.findViewById(R.id.etDireccion);
        EditText etMunicipio = view.findViewById(R.id.etMunicipio);
        EditText etNotas = view.findViewById(R.id.etNotas);

        if (clienteAEditar != null) {
            tvTitulo.setText("Editar Cliente");
            etNombre.setText(clienteAEditar.nombre);
            etTelefono.setText(clienteAEditar.telefono);
            etEmail.setText(clienteAEditar.email);
            etDireccion.setText(clienteAEditar.direccion);
            etMunicipio.setText(clienteAEditar.municipio);
            etNotas.setText(clienteAEditar.notas);
        }

        builder.setView(view)
                .setPositiveButton("Guardar", (dialog, id) -> {
                    String nombre = etNombre.getText().toString();
                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "Nombre obligatorio", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    Cliente c = (clienteAEditar == null) ? new Cliente() : clienteAEditar;
                    c.nombre = nombre;
                    c.telefono = etTelefono.getText().toString();
                    c.email = etEmail.getText().toString();
                    c.direccion = etDireccion.getText().toString();
                    c.municipio = etMunicipio.getText().toString();
                    c.notas = etNotas.getText().toString();
                    
                    if (clienteAEditar == null) db.appDao().insertarCliente(c);
                    else db.appDao().actualizarCliente(c);
                    
                    cargarDatos("clientes");
                    actualizarDashboard();
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
                        s.costoManoObra = 0; s.costoMateriales = 0;
                    }
                    db.appDao().insertarServicio(s);
                    cargarDatos("servicios");
                    actualizarDashboard();
                })
                .setNegativeButton("Cancelar", null).show();
    }

    private void mostrarDetalleCliente(Cliente c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.dialog_detalle_cliente, null);

        ((TextView) view.findViewById(R.id.tvDetalleNombre)).setText(c.nombre);
        ((TextView) view.findViewById(R.id.tvDetalleTelefono)).setText(c.telefono);
        ((TextView) view.findViewById(R.id.tvDetalleEmail)).setText(c.email);
        ((TextView) view.findViewById(R.id.tvDetalleDireccion)).setText(c.direccion);
        ((TextView) view.findViewById(R.id.tvDetalleMunicipio)).setText(c.municipio);
        ((TextView) view.findViewById(R.id.tvDetalleNotas)).setText(c.notas);

        AlertDialog dialog = builder.setView(view).create();

        view.findViewById(R.id.btnLlamar).setOnClickListener(v -> {
            if (c.telefono != null && !c.telefono.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + c.telefono));
                startActivity(intent);
            } else {
                Toast.makeText(this, "No hay número registrado", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnWhatsapp).setOnClickListener(v -> {
            if (c.telefono != null && !c.telefono.isEmpty()) {
                String url = "https://api.whatsapp.com/send?phone=" + c.telefono;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } else {
                Toast.makeText(this, "No hay número registrado", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
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
                ClienteViewHolder cvh = (ClienteViewHolder) holder;
                cvh.nombre.setText(c.nombre);
                cvh.tel.setText(c.telefono);
                cvh.email.setText(c.email);
                
                holder.itemView.setOnClickListener(v -> mostrarDetalleCliente(c));
                cvh.btnEditar.setOnClickListener(v -> mostrarDialogoNuevoCliente(c));
                
                cvh.btnEliminar.setOnClickListener(v -> {
                    int servicios = db.appDao().contarServiciosDeCliente(c.id);
                    if (servicios > 0) {
                        Toast.makeText(MainActivity.this, "No se puede eliminar: tiene " + servicios + " servicios registrados", Toast.LENGTH_LONG).show();
                    } else {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Eliminar Cliente")
                            .setMessage("¿Deseas eliminar a " + c.nombre + "?")
                            .setPositiveButton("Eliminar", (d, w) -> {
                                db.appDao().eliminarCliente(c);
                                cargarDatos("clientes");
                                actualizarDashboard();
                            })
                            .setNegativeButton("Cancelar", null).show();
                    }
                });
            } else {
                ServicioConCliente sc = (ServicioConCliente) item;
                Servicio s = sc.servicio;
                ServicioViewHolder svh = (ServicioViewHolder) holder;
                svh.clienteNombre.setText(sc.nombreCliente);
                svh.tipo.setText(s.tipoServicio);
                svh.estado.setText(s.estado);
                svh.idC.setText("Total: $" + (s.costoManoObra + s.costoMateriales));

                svh.btnEliminar.setOnClickListener(v -> {
                    if (s.estado.equals("Pendiente")) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Eliminar servicio")
                                .setMessage("¿Deseas eliminar este servicio?")
                                .setPositiveButton("Eliminar", (d, w) -> {
                                    db.appDao().eliminarServicio(s);
                                    cargarDatos("servicios");
                                    actualizarDashboard();
                                })
                                .setNegativeButton("Cancelar", null).show();
                    } else {
                        Toast.makeText(MainActivity.this, "No se puede eliminar un servicio en " + s.estado, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
        @Override
        public int getItemCount() { return dataList.size(); }
    }

    static class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, tel, email;
        ImageButton btnEditar, btnEliminar;
        ClienteViewHolder(View v) { 
            super(v); 
            nombre=v.findViewById(R.id.tvNombreCliente); 
            tel=v.findViewById(R.id.tvTelefonoCliente); 
            email=v.findViewById(R.id.tvEmailCliente);
            btnEditar=v.findViewById(R.id.btnEditarCliente);
            btnEliminar=v.findViewById(R.id.btnEliminarCliente);
        }
    }

    static class ServicioViewHolder extends RecyclerView.ViewHolder {
        TextView tipo, estado, idC, clienteNombre;
        ImageButton btnEliminar;
        ServicioViewHolder(View v) { 
            super(v); 
            tipo=v.findViewById(R.id.tvTipoServicio); 
            estado=v.findViewById(R.id.tvEstadoServicio); 
            idC=v.findViewById(R.id.tvClienteId); 
            clienteNombre=v.findViewById(R.id.tvClienteNombre); 
            btnEliminar=v.findViewById(R.id.btnEliminarServicio);
        }
    }
}