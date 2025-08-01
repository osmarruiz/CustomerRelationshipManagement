import React, { useEffect } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Badge, Button, Table } from 'reactstrap';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getGatewayRoutes } from '../administration.reducer';

export const GatewayPage = () => {
  const dispatch = useAppDispatch();
  const isFetching = useAppSelector(state => state.administration.loading);
  const routes = useAppSelector(state => state.administration.gateway.routes);

  useEffect(() => {
    dispatch(getGatewayRoutes());
  }, []);

  const metadata = instance => {
    const spans = [];
    Object.keys(instance).map((key, index) => {
      spans.push(
        <span key={`${key.toString()}value`}>
          <Badge key={`${key.toString()}-containerbadge`} className="fw-normal">
            <Badge key={`${key.toString()}-badge`} color="info" className="fw-normal" pill>
              {key}
            </Badge>
            {instance[key]}
          </Badge>
        </span>,
      );
    });
    return spans;
  };

  const badgeInfo = info => {
    if (info) {
      if (info.checks && info.checks.filter(check => check.status === 'PASSING').length === info.checks.length) {
        return <Badge color="success">UP</Badge>;
      }
      return <Badge color="danger">DOWN</Badge>;
    }
    return <Badge color="warning">?</Badge>;
  };

  const instanceInfo = route => {
    if (route) {
      return (
        <Table striped responsive>
          <tbody>
            {route.serviceInstances.map((instance, i) => (
              <tr key={`${instance.instanceInfo}-info`}>
                <td>
                  <a href={instance.uri} target="_blank" rel="noopener noreferrer">
                    {instance.uri}
                  </a>
                </td>
                <td>{badgeInfo(instance.healthService)}</td>
                <td>{metadata(instance.metadata)}</td>
              </tr>
            ))}
          </tbody>
        </Table>
      );
    }
  };

  const gatewayRoutes = () => {
    if (!isFetching) {
      dispatch(getGatewayRoutes());
    }
  };

  return (
    <div>
      <h2>Gateway</h2>
      <p>
        <Button onClick={gatewayRoutes} color={isFetching ? 'danger' : 'primary'} disabled={isFetching}>
          <FontAwesomeIcon icon="sync" />
          &nbsp; Refrescar
        </Button>
      </p>

      <Table striped responsive>
        <thead>
          <tr key="header">
            <th>URL</th>
            <th>servicio</th>
            <th>Servidores Disponibles</th>
          </tr>
        </thead>
        <tbody>
          {routes.map((route, i) => (
            <tr key={`routes-${i}`}>
              <td>{route.path}</td>
              <td>{route.serviceId}</td>
              <td>{instanceInfo(route)}</td>
            </tr>
          ))}
        </tbody>
      </Table>
    </div>
  );
};

export default GatewayPage;
