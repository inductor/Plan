import React from 'react';
import {useServer} from "../layout/ServerPage";
import ErrorView from "../ErrorView";
import LoadIn from "../../components/animation/LoadIn";
import {Card, Col, Row} from "react-bootstrap-v5";
import ExtensionCard from "../../components/extensions/ExtensionCard";
import {useParams} from "react-router-dom";

const ServerWidePluginData = () => {
    const {plugin} = useParams();
    const {extensionData, extensionDataLoadingError} = useServer();

    if (extensionDataLoadingError) return <ErrorView error={extensionDataLoadingError}/>;

    const extension = extensionData?.find(extension => extension.extensionInformation.pluginName === plugin)

    if (!extension) {
        return (
            <LoadIn>
                <section className="server_plugin_data">
                    <Row style={{overflowY: 'hidden'}}>
                        <Col md={12}>
                            <Card>
                                <Card.Body>
                                    <p>No Extension data</p>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </section>
            </LoadIn>
        )
    }

    return (
        <LoadIn>
            <section className="server_plugin_data">
                <Row id={"extension-" + extension.extensionInformation.pluginName}
                     style={{overflowY: 'hidden'}}>
                    <Col md={12}>
                        <ExtensionCard extension={extension}/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
};

export default ServerWidePluginData