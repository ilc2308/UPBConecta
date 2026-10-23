import { useAuth } from '../context/AuthContext';
import './Header.css';

export function Header() {
  const { usuario, cerrarSesion } = useAuth();

  return (
    <header className="header">
      <div className="header__contenido">
        <span className="header__marca">
          UPB <strong>Conecta</strong>
        </span>
        {usuario && (
          <div className="header__usuario">
            <span>{usuario.nombre}</span>
            <button className="boton boton-secundario" onClick={cerrarSesion}>
              Cerrar sesión
            </button>
          </div>
        )}
      </div>
    </header>
  );
}
