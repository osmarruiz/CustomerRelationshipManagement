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
    <div>
      <div className="container-fluid pt-5">
        <div className="row mb-4 mb-lg-5 justify-content-lg-between">
          <div className="col-3 col-md-1 col-lg-2 d-none d-md-flex align-items-center">
            <div className="lc-block bg-dark ratio ratio-1x1 opacity-25"> </div>
          </div>
          <div className="col-4 col-md-3 col-lg-2 d-flex flex-column justify-content-between">
            <div className="lc-block bg-primary ratio ratio-1x1 opacity-25"> </div>
            <div className="lc-block">
              <img
                className="img-fluid"
                src="https://images.unsplash.com/photo-1531445075774-bfb641f42229?crop=entropy&amp;cs=tinysrgb&amp;fit=crop&amp;fm=jpg&amp;ixid=MnwzNzg0fDB8MXxzZWFyY2h8Njd8fGJ1aWxkaW5nfGVufDB8Mnx8fDE2MzQ1NTE3NDg&amp;ixlib=rb-1.2.1&amp;q=80&amp;w=1080&amp;h=1080"
                alt="Photo by Meriç Dağlı"
              />
            </div>
          </div>
          <div className="col-4 col-md-4 col-lg-3">
            <img
              className="img-fluid"
              src="https://images.unsplash.com/photo-1526546334624-2afe5b01088d?crop=entropy&amp;cs=tinysrgb&amp;fit=max&amp;fm=jpg&amp;ixid=MnwzNzg0fDB8MXxzZWFyY2h8MzF8fGJ1aWxkaW5nfGVufDB8MXx8fDE2MzQ1NTE2NTE&amp;ixlib=rb-1.2.1&amp;q=80&amp;w=1080"
              style={{ objectFit: 'cover' }}
              alt="Photo by Simone Hutsch"
            />
          </div>
          <div className="col-4 col-md-3 col-lg-2 d-flex flex-column justify-content-between">
            <div className="lc-block">
              <img
                className="img-fluid"
                src="https://images.unsplash.com/photo-1528810084506-41bd091551af?crop=entropy&amp;cs=tinysrgb&amp;fit=crop&amp;fm=jpg&amp;ixid=MnwzNzg0fDB8MXxzZWFyY2h8NDJ8fGJ1aWxkaW5nfGVufDB8Mnx8fDE2MzQ1NTE3NDE&amp;ixlib=rb-1.2.1&amp;q=80&amp;w=1080&amp;h=1080"
                alt="Photo by Simone Hutsch"
              />
            </div>
            <div className="lc-block bg-primary ratio ratio-1x1 opacity-25"> </div>
          </div>
          <div className="col-3 col-md-1 col-lg-2 d-none d-md-flex align-items-center">
            <div className="lc-block bg-dark ratio ratio-1x1 opacity-25"> </div>
          </div>
        </div>
      </div>
      <div className="container">
        <div className="row justify-content-center">
          <div className="lc-block text-center col-md-8">
            <div>
              <h1 className="rfs-25 fw-bold">Bienvenido a la aplicación CRM</h1>
            </div>
          </div>
        </div>
        <div className="row justify-content-center">
          <div className="lc-block text-center col-xxl-6 col-md-8">
            <div>
              <p className="lead">
                {' '}
                Administra las relaciones con tus clientes de manera efectiva y eficiente con nuestras herramientas e información integral.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
