import './home.scss';

import React, { useEffect } from 'react';
import { Helmet } from 'react-helmet';
import { useLocation, useNavigate } from 'react-router-dom';

import { Alert } from 'reactstrap';

import { REDIRECT_URL, getLoginUrl } from 'app/shared/util/url-utils';
import { useAppSelector } from 'app/config/store';

export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);
  const pageLocation = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const redirectURL = localStorage.getItem(REDIRECT_URL);
    if (redirectURL) {
      localStorage.removeItem(REDIRECT_URL);
      location.href = `${location.origin}${redirectURL}`;
    }
  }, []);

  return (
    <div className="crm-hero-container">
      <Helmet>
        <title>ConectaCRM - Tu espacio para conectar, crecer y fidelizar clientes</title>
        <meta name="description" content="ConectaCRM: conecta, crece y fideliza clientes fácilmente." />
      </Helmet>
      <div className="crm-hero-bg">
        <div className="crm-hero-overlay" />
        <div className="crm-hero-content">
          <div className="crm-hero-icon">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="12" fill="#43a047" />
              <path d="M7 17v-2a4 4 0 018 0v2" stroke="#fff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              <circle cx="12" cy="9" r="3" fill="#fff" />
            </svg>
          </div>
          <h1 className="crm-hero-title">¡Bienvenido a ConectaCRM!</h1>
          <p className="crm-hero-subtitle">Tu espacio para conectar, crecer y fidelizar clientes con facilidad</p>
          {account?.login ? (
            <Alert color="success" className="crm-hero-alert">
              ¡Hola <b>{account.login}</b>! Nos alegra verte de nuevo 😊
            </Alert>
          ) : (
            <Alert color="info" className="crm-hero-alert">
              <span>¿Tienes cuenta?</span>{' '}
              <a
                className="crm-hero-login-link"
                onClick={() =>
                  navigate(getLoginUrl(), {
                    state: { from: pageLocation },
                  })
                }
                style={{ cursor: 'pointer', marginLeft: 8 }}
              >
                Inicia sesión aquí
              </a>
              <br />
              <span style={{ fontSize: '0.9em', color: '#555' }}>
                Puedes probar con: <b>admin/admin</b> o <b>user/user</b>
              </span>
            </Alert>
          )}
          <div className="crm-hero-links">
            <a href="#" className="crm-hero-btn">
              Clientes
            </a>
            <a href="#" className="crm-hero-btn crm-hero-btn-secondary">
              Reportes
            </a>
            <a href="#" className="crm-hero-btn crm-hero-btn-tertiary">
              Soporte
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
