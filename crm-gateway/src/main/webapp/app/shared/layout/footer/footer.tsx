import './footer.scss';

import React from 'react';

import { Col, Row, Container } from 'reactstrap';

const Footer = () => (
  <footer className="footer bg-primary text-white py-4">
    <Container>
      <Row className="text-center">
        <Col md="12">
          <p className="mb-0">&copy; 2025 Customer Relationship Management. Todos los derechos reservados.</p>
        </Col>
      </Row>
    </Container>
  </footer>
);

export default Footer;
